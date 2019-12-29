# idea-plugin-shadowserve

An IntellJ IDEA plugin to run a dev server for serving local web files, shadowing resources from an existing web server.


### Requirements

1. IntelliJ IDEA 2019.3 (or latest) IDE, running on Java 11 (or latest) runtime.


### Use case

This plugin makes it easier to edit and debug web resources from a web server, inside the IDE, while being able to 
preview the results in a browser. It is easy and quick to overlay some local web files over a deployed server and 
preview how the changes would look when deployed, as rest of the calls are forwarded to same web server.

It does so by runs a local server that overlays (shadow) local files over some (or all) files coming from a source 
web server.

![Example Diagram](https://raw.githubusercontent.com/codebysd/idea-plugin-shadowserve/master/docs/diagram.png)

In above scenario, `scripts/app.js` is being edited and served locally, and previewed with other files from a remote 
server. 

### Usage

The plugin adds a run configuration of type `Shadow Serve`. The configuration options are as follows:

1. `Local Server Port` : The local port on which shadow server will run.
2. `Origin Web URL` : The URL of remote or local server that serves all other files not replaced by local files.
3. `Shadow Path` : The path on origin web server, that is to be shadowed. All requests starting with this path, will
   cause files to be looked up from local project.
4. `Local Root Directory` : The parent directory under which shadowed files will be searched.

After starting the run configuration, open browser at `http://localhost:<local-port>` to preview web results.

#### With JS Debugger
To not only serve but also debug Javascript, the above run configuration can be added as a pre-task to the JS Debugger
run configuration available in IDE.
 

