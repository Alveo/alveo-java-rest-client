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
