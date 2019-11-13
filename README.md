An adapter of Jenkins update center.

## Why

Jenkins downloads the plugins by parsing the JSON file which comes from a update center.
One thing that you might already noticed is about the speed of downloading.
Accessing a global storage for everyone could be slow although 
there're many [mirror sites](http://mirrors.jenkins-ci.org/status.html).

If you deep into the file [update-center.json](https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json).
Almost every mirror file is base on `http://updates.jenkins-ci.org/download/plugins`.
So, the result is that these mirror sites can only speed up the process of downloading file update-center.json.
Jenkins can download the `.hpi` from your target mirror site.

## Background

It's might not be a good idea to change the `update-center.json` file directly. keeping sync all files
should be simple without other logic.

Second, you need to provide a certificate file if you changed the `update-center.json`. Because Jenkins
will validate the file before parsing it. It's necessary due to the safety reason.

## Design

* Create an adapter to replace the base URL
* Provide a certificate file

## How to

Here's a prototype implement which added into [localization-zh-cn-plugin](https://github.com/jenkinsci/localization-zh-cn-plugin/pull/115).
You Just need to take three steps if you want to use a real mirror of update center:

* install localization-zh-cn-plugin 1.0.10
* use the new certificate file
* change the update center URL

## Feedback

Please don't hesitate to tell us your thoughts.
