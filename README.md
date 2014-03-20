# HCS vLab Client

A Java interface to [HCS vLab][hvl].

[hvl]: http://hcsvlab.org.au/

## Purpose

This package provides a Java wrapper for the REST API of HCS vLab.

## Building

The project uses a fairly standard Maven build setup. Build using

    $ mvn compile

## Usage

To access the REST API, instantiate `com.nicta.vlabclient.RestClient`.
The class `com.nicta.vlabclient.examples.RestClientExample` is fairly
simple command line client. Invoke with no arguments for a usage
message. The code of the class also provides an example of API usage.

## Testing

To run the test suite off the live server, you will need to configure
the location of the server and the API key. Create a file at
`src/test/resources/application.conf` in [HOCON format][hocon] modelled after
`src/test/resources/example.conf` -- that is, with three keys:

[hocon]: https://github.com/typesafehub/config/blob/master/HOCON.md
 * `vlabclient.test.run-live` -- whether to run the live tests
 * `vlabclient.test.server-base` -- the base URL of the HCS vLab server
 * `vlabclient.test.api-key` -- the API key

If you don't set this up, only the recorded tests will run, although
this is probably OK.