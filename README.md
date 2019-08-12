# goos

A Scala implementation of ["Growing Object-Oriented Software, Guided by Tests" (Nat Pryce & Steve Freeman)](https://www.amazon.com/Growing-Object-Oriented-Software-Guided-Tests/dp/0321503627)

## Setup

To build the project you'll have to be in the Wix network. This is because the specs2-jmock integration is not yet public.

### Setting up Openfire

Download the openfire tarball [here](http://www.igniterealtime.org/downloads/download-landing.jsp?file=openfire/openfire_3_9_3.tar.gz).

Expand:
```
tar zxf openfire_3_9_3.tar.gz
```

And run:
```
bin/openfire start
```

Then setup the server using the instructions in the book.

NOTE - If you're using Mac, do NOT put hyphens in your chat names in openfire (item-12345), since the client doesn't handle them well. Just use underscore or ommit them altogether.
