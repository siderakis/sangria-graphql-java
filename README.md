
## Roadmap

Modify the sangria playground to using GraphQL-Java for query execution with minimal changes to the schema creation code.

1. [WIP] Print schema (all types, enums, scalars, etc), without the resolvers (can't execute a query).
2. Try to implement the resolvers. Probably Fail. Learn.
3. Fork sangria to just the schema creation code.
4. Replace Action class with a light weight resolvers function.
5. Get sample fully working with new fork.
6. Publish forked code as a new library.


Open questions: Relay support.

### Compare rendered schema
```bash
$ git clone https://github.com/siderakis/sangria-graphql-java.git
$ cd sangria-graphql-java/sangria-playground
$ sbt run
```

 - Sangria schema rendered: http://localhost:9000/render-schema
 - GraphQL-Java schema rendered: http://localhost:9000/render-java-schema

#### Sangria Schema

```
"A character in the Star Wars Trilogy"
interface Character {
  "The id of the character."
  id: String!

  "The name of the character."
  name: String

  "The friends of the character, or an empty list if they have none."
  friends: [Character!]!

  "Which movies they appear in."
  appearsIn: [Episode]
}

"A mechanical creature in the Star Wars universe."
type Droid implements Character {
  "The id of the droid."
  id: String!

  "The name of the droid."
  name: String

  "The friends of the droid, or an empty list if they have none."
  friends: [Character!]!

  "Which movies they appear in."
  appearsIn: [Episode]

  "The primary function of the droid."
  primaryFunction: String
}

"One of the films in the Star Wars Trilogy"
enum Episode {
  "Released in 1977."
  NEWHOPE

  "Released in 1980."
  EMPIRE

  "Released in 1983."
  JEDI
}

"A humanoid creature in the Star Wars universe."
type Human implements Character {
  "The id of the human."
  id: String!

  "The name of the human."
  name: String

  "The friends of the human, or an empty list if they have none."
  friends: [Character!]!

  "Which movies they appear in."
  appearsIn: [Episode]

  "The home planet of the human, or null if unknown."
  homePlanet: String
}

type Query {
  hero(
    "If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode."
    episode: Episode): Character! @deprecated(reason: "Use `human` or `droid` fields instead")
  human(
    "id of the character"
    id: String!): Human
  droid(
    "id of the character"
    id: String!): Droid!
}

```
#### Sangria-GraphQL-Java Schema

```
#A character in the Star Wars Trilogy
interface Character {
  #Which movies they appear in.
  appearsIn: [Episode]
  #The friends of the character, or an empty list if they have none.
  friends: [Character]
  #The id of the character.
  id: String!
  #The name of the character.
  name: String
}

#A mechanical creature in the Star Wars universe.
type Droid implements Character {
  #Which movies they appear in.
  appearsIn: [Episode]
  #The friends of the character, or an empty list if they have none.
  friends: [Character]
  #The id of the character.
  id: String!
  #The name of the character.
  name: String
  #The primary function of the droid.
  primaryFunction: String
}

#A humanoid creature in the Star Wars universe.
type Human implements Character {
  #Which movies they appear in.
  appearsIn: [Episode]
  #The friends of the character, or an empty list if they have none.
  friends: [Character]
  #The home planet of the human, or null if unknown.
  homePlanet: String
  #The id of the character.
  id: String!
  #The name of the character.
  name: String
}

type Query {
  droid(
    #id of the character
    id: String!
  ): Droid
  hero(
    #If omitted, returns the hero of the whole saga. If provided, returns the hero of that particular episode.
    episode: Episode
  ): Character
  human(
    #id of the character
    id: String!
  ): Human
}

#One of the films in the Star Wars Trilogy
enum Episode {
  #Released in 1980.
  EMPIRE
  #Released in 1983.
  JEDI
  #Released in 1977.
  NEWHOPE
}
```


## Original doc
### Sangria playground

This is an example of a [GraphQL](https://facebook.github.io/graphql) server written with [Play framework](https://www.playframework.com) and
[Sangria](http://sangria-graphql.org). It also serves as a playground. On the right hand side you can see a textual representation of the GraphQL
schema which is implemented on the server and that you can query here. On the left hand side
you can execute a GraphQL queries and see the results of its execution.

It's available here:

[http://try.sangria-graphql.org](http://try.sangria-graphql.org)

This is just a small demonstration. It really gets interesting when you start to play with the schema on the server side. Fortunately it's
pretty easy to do. Since it's a simple Play application, all it takes to start playground locally and start playing with the schema is this:

```bash
$ git clone https://github.com/sangria-graphql/sangria-playground.git
$ cd sangria-playground
$ sbt run
```

Now you are ready to point your browser to [http://localhost:9000](http://localhost:9000).
The only prerequisites are [SBT](http://www.scala-sbt.org/download.html) and [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
