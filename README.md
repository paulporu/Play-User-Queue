# Play-User-Queue

[![Build Status](https://travis-ci.org/paulporu/Play-User-Queue.svg?branch=master)](https://travis-ci.org/paulporu/Play-User-Queue)


This REST API allows for flexible management of users in waiting queues. It was built on top of Play Scala and MongoDB, and illustrates how this custom [Priority Queue implementation](https://github.com/paulporu/flexible-user-queue) can be used in Scala projects. 

## Setup

1. Install [MongoDB](https://docs.mongodb.com/manual/installation/#tutorials). 
2. [Download Typesafe Activator](http://typesafe.com/platform/getstarted)
3. Extract the zip and run the `activator` script from a non-interactive shell
4. Your browser should open to the Activator UI: [http://localhost:8888](http://localhost:8888)

## Run the App

Using the Activator UI you can compile the code, run tests, and run the app. You can see the running application at: [http://localhost:9000/](http://localhost:9000/)

You can also use the command line:   
> $ activator ~run     
> $ activator ~test

For more information you can visit: [https://www.playframework.com/documentation/2.5.x/PlayConsole](https://www.playframework.com/documentation/2.5.x/PlayConsole)

## Usage


## Dependencies

The [ReactiveMongo plugin](http://reactivemongo.org/releases/0.11/documentation/tutorial/play2.html) for Play was used as the Scala driver for MongoDB.   
The [ScalaTest + Play](http://www.scalatest.org/plus/play) integration library was used for testing the Play application with [Scalatest](http://www.scalatest.org/).    
[Scalastyle](http://www.scalastyle.org/) was used because it's always good to follow some guidelines. 

## Stylistic Note: 

In this application, pattern matching was usually chosen over map.getOrElse or fold for readability purposes. As a personal preference I often use the first two interchangeably but fold does seem to produce less readable code in most cases. Also see: https://www.reddit.com/r/scala/comments/449td4/fold_or_pattern_matching_with_an_option/

## License

MIT License
