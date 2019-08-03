package sangriatographqljava

import java.util.function.UnaryOperator

import graphql.Scalars
import graphql.schema._
import models.CharacterRepo
import sangria.schema.{Argument, CompositeType, EnumType, Field, InputObjectType, InputType, InterfaceType, ListInputType, ListType, ObjectLikeType, ObjectType, OptionInputType, OptionType, OutputType, ScalarAlias, ScalarType, Schema, UnionType}

import scala.collection.JavaConverters._
import scala.collection.mutable

object SchemaConverter {
  def scalaToJavaSchema(scalaSchema: Schema[CharacterRepo, Unit]) = {

    val converter = SchemaConverter()

    val query: GraphQLObjectType = converter.scalaToJavaObject(scalaSchema.query)
    val schemaBuilder = GraphQLSchema.newSchema().query(query)
    scalaSchema.mutation.foreach(mutation => schemaBuilder.mutation(converter.scalaToJavaObject(mutation)))

    schemaBuilder.additionalTypes(converter.objectMap.values.toSet.asJava)
    schemaBuilder.build()
  }


}

case class SchemaConverter(objectMap: mutable.LinkedHashMap[String, GraphQLType] = mutable.LinkedHashMap[String, GraphQLType]()
                          ) {

  def createOutputType(fieldType: OutputType[_]): GraphQLOutputType = {

    fieldType match {

      case ScalarType(name, description, coerceUserInput, coerceOutput, coerceInput, complexity, scalarInfo, astDirectives, astNodes) =>
        GraphQLNonNull.nonNull(Scalars.GraphQLString)
      case ScalarAlias(aliasFor, toScalar, fromScalar) =>
        GraphQLNonNull.nonNull(Scalars.GraphQLString)
      case obj: ObjectLikeType[_, _] if (obj.isInstanceOf[InterfaceType[_, _]]) =>
        if (!objectMap.contains(obj.name)) {
          objectMap.put(obj.name, null)
          val interface = obj.asInstanceOf[InterfaceType[_, _]]
          val interfaceType = GraphQLInterfaceType.newInterface().
            name(obj.name).
            typeResolver(env => GraphQLTypeReference.typeRef(interface.manualPossibleTypes().head.name /*TODO*/).asInstanceOf[GraphQLObjectType]).
            description(interface.description.getOrElse(null))

          interfaceType.fields(obj.fields.map(convertField).asJava)
          objectMap.put(obj.name, interfaceType.build())
        }
        GraphQLTypeReference.typeRef(obj.name)
      case obj: ObjectLikeType[_, _] =>
        if (!objectMap.contains(obj.name)) {
          objectMap.put(obj.name, null)
          val objType =
            GraphQLObjectType.newObject().name(fieldType.namedType.name)
          fieldType.namedType.description.foreach(objType.description)
          obj.interfaces.foreach(i => objType.withInterface(GraphQLTypeReference.typeRef(i.name)))
          objType.fields(obj.fields.map(convertField).asJava)
          objectMap.put(obj.name, objType.build())
        }
        GraphQLTypeReference.typeRef(obj.name)
      case _: CompositeType[_] =>
        val objType =
          GraphQLObjectType.newObject().name(fieldType.namedType.name)

        fieldType.namedType.description.foreach(objType.description)
        objType.build()
      case UnionType(name, description, types, astDirectives, astNodes) =>
        GraphQLUnionType.newUnionType().
          name(name).
          description(description.getOrElse(null)).
          //          possibleType().
          build()
      case EnumType(name, description, values, astDirectives, astNodes) =>
        if (!objectMap.contains(name)) {
          val newType = GraphQLEnumType.newEnum().
            name(name).
            description(description.getOrElse(null)).
            values(values.map(value => GraphQLEnumValueDefinition.newEnumValueDefinition().name(value.name).description(value.description.getOrElse(null)).value(value.value).build()).asJava).
            build()
          objectMap.put(name, newType)
        }
        GraphQLTypeReference.typeRef(name)
      case ListType(ofType) =>
        GraphQLList.list(createOutputType(ofType))
      case OptionType(ofType) =>
        GraphQLTypeUtil.unwrapNonNull(createOutputType(ofType)).asInstanceOf[GraphQLOutputType]
    }
  }

  def createInputType(argumentType: InputType[_]): GraphQLInputType = {

    argumentType match {
      case ScalarType(name, description, coerceUserInput, coerceOutput, coerceInput, complexity, scalarInfo, astDirectives, astNodes) =>
        //TODO:        GraphQLScalarType.newScalar().name(name).description(description.getOrElse("")).
        GraphQLNonNull.nonNull(Scalars.GraphQLString)
      case ScalarAlias(aliasFor, toScalar, fromScalar) =>
        GraphQLNonNull.nonNull(Scalars.GraphQLString)
      case EnumType(name, description, values, astDirectives, astNodes) =>
        if (!objectMap.contains(name)) {
          val newType = GraphQLEnumType.newEnum().
            name(name).
            description(description.getOrElse(null)).
            values(values.map(value => GraphQLEnumValueDefinition.newEnumValueDefinition().name(value.name).description(value.description.getOrElse(null)).value(value.value).build()).asJava).
            build()
          objectMap.put(name, newType)
        }
        GraphQLTypeReference.typeRef(name)
      case InputObjectType(name, description, fieldsFn, astDirectives, astNodes) =>
        GraphQLNonNull.nonNull(Scalars.GraphQLString)

      case ListInputType(ofType) =>
        GraphQLList.list(createInputType(ofType))
      case OptionInputType(ofType) =>
        GraphQLTypeUtil.unwrapNonNull(createInputType(ofType)).asInstanceOf[GraphQLInputType]
    }
  }

  private def scalaToJavaObject(objectType: ObjectType[CharacterRepo, Unit]): GraphQLObjectType = {
    val queryBuilder = GraphQLObjectType.newObject().name(objectType.name)
    objectType.description.foreach(queryBuilder.description)
    queryBuilder.fields(objectType.fields.map(convertField).asJava)
    queryBuilder.build()
  }

  private def convertField(scalaField: Field[_, _]): GraphQLFieldDefinition = {


    val fieldBuilder =
      GraphQLFieldDefinition.newFieldDefinition().name(scalaField.name)

    scalaField.deprecationReason.foreach(fieldBuilder.deprecate)
    scalaField.description.foreach(fieldBuilder.description)

    fieldBuilder.dataFetcher(env => {
      // TODO: fix me
      //        val action:Action[Any,_] = scalaField.resolve(env)

      null
    })

    fieldBuilder.arguments(scalaField.arguments.map(convertArg).asJava)


    fieldBuilder.`type`(createOutputType(scalaField.fieldType))
    fieldBuilder.build()
  }

  private def convertArg(scalaArg: Argument[_]): GraphQLArgument = {
    val argBuilder =
      GraphQLArgument.newArgument().name(scalaArg.name)
    scalaArg.defaultValue.foreach(argBuilder.defaultValue)
    scalaArg.description.foreach(argBuilder.description)

    val inputObjType: GraphQLInputType = createInputType(scalaArg.argumentType)

    argBuilder.`type`(inputObjType)

    argBuilder.build()

  }
}
