# openEQUELLA

[![openEQUELLA CI](https://github.com/openequella/openEQUELLA/workflows/openEQUELLA%20CI/badge.svg?branch=develop)](https://github.com/openequella/openEQUELLA/actions?query=workflow%3A%22openEQUELLA+CI%22+branch%3Adevelop)

openEQUELLA is a digital repository that provides a single platform to house your teaching and
learning, research, media, and library content.

Builds for each openEQUELLA Release can be found on the [Releases
page](https://github.com/openequella/openEQUELLA/releases "EQUELLA Releases"). The latest stable versions
(with their changelogs) can also be retrieved from the version server at
<https://version.openequella.net/>.

(NOTE: The current stable version - starting from 2019.1 - is built from `master`, where as active
development is undertaken on the repository's default branch `develop`. Therefore `develop` is
considered the project's 'unstable' branch.)

The project's homepage and documentation can be found at <https://openequella.github.io/>.

If you would like to contribute to openEQUELLA please review the [Contributor
Guidelines](CONTRIBUTING.md) - which also include details of how to get in touch. We welcome pull
requests and issue reports. And if you'd like to assist with documentation, please head on over to
the documentation repository at <https://github.com/openequella/openEQUELLA.github.io>.

Below you'll find further information for developers wishing to work with the source code.

# Building openEQUELLA from source

- [Download required software](#download-required-software)
- [Get the code](#get-the-code)
- [Build installer](#building-the-installer)

## Download required software

**Download and install Git**

<https://git-scm.com/downloads>

In ubuntu:

```bash
~$ sudo apt-get install git
```

**SSH**

This guide assumes you have SSH capabilities. Be sure to add your public SSH key into the you git profile to access the code repos.

**Download and install SBT**

<https://www.scala-sbt.org/>

In ubuntu:

```bash
~$ echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
~$ sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
~$ sudo apt-get update
~$ sudo apt-get install sbt
```

**Install Node/NPM**

<https://nodejs.org/>

As of the time of writing the build was tested Node v12.16.1 and NPM v6.13.4.
The recommended version of Node is noted in [_.nvmrc_](./.nvmrc), this version can be installed with [nvm](https://github.com/nvm-sh/nvm) using `nvm install`,

**Download and install Java 8 JDK**

<https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html>

Oracle is the recommended and historically the supported vendor of Java to run openEquella with.

In ubuntu:

```bash
~$ sudo add-apt-repository ppa:webupd8team/java
~$ sudo apt-get update
~$ sudo apt-get install oracle-java8-installer
```

**Download and install Image Magick binaries**

<https://imagemagick.org/script/download.php>

_Note: For ubuntu follow the install from Unix Source instructions:_
<https://imagemagick.org/script/install-source.php>

To confirm the installation directory in Ubuntu for the openEquella installer, run the command:

```bash
~$ whereis convert
```

When installing in Windows, check “Install Legacy Utilities (e.g. convert)”.

**Download and install libav**

In ubuntu:

```bash
~$ sudo apt-get install libav-tools
```

To confirm the installation directory in Ubuntu for the openEquella installer, run the command:

```bash
~$ whereis avconv
```

Once SBT and Java are installed, you may need to set a JAVA_HOME environment variable.

**Database**

- Either [PostgreSQL](https://www.postgresql.org/), SQLServer, or Oracle database.

## Get the code

### Base code

**Git Clone**

```bash
~$ git clone git@github.com:openequella/openEQUELLA.git
```

### Optional code

There is functionality that could not be included into the core openEquella code repository, but based on your business needs, may be appropriate to include.

- Oracle DB Driver
- [Kaltura](https://github.com/equella/Equella-Kaltura)
