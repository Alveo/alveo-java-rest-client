# Alveo REST API Client

A Java interface to [Alveo][alv].

[alv]: http://alveo.edu.au/

## Purpose

This package provides a Java wrapper for the REST API of Alveo.

## Building

The project uses a fairly standard Maven build setup. Build using

    $ mvn compile

## Usage

To access the REST API, instantiate `au.edu.alveo.client.RestClient`.
The class `au.edu.alveo.client.examples.RestClientExample` is fairly
simple command line client. Invoke with no arguments for a usage
message. The code of the class also provides an example of API usage.

## Testing

To run the test suite off the live server, you will need to configure
the location of the server and the API key. Create a file at
`src/test/resources/application.conf` in [HOCON format][hocon] modelled after
`src/test/resources/example.conf` -- that is, with three keys:

[hocon]: https://github.com/typesafehub/config/blob/master/HOCON.md
 * `alveo-rest-client.test.run-live` -- whether to run the live tests
 * `alveo-rest-client.test.server-base` -- the base URL of the Alveo server
 * `alveo-rest-client.test.api-key` -- the API key

If you don't set this up, only the recorded tests will run, although
this is probably OK.