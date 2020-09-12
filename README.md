# react-native-init-shadow

Initialize shadow-cljs projects to target react-native. This project
uses the existing `react-native-init` cli to generate the project's
base, and will generate the project using the latest stable
react-native version.

## Dependencies

In order for the react-native project generator to work, you must have
already installed
[shadow-cljs](https://github.com/thheller/shadow-cljs),
[react-native](https://reactnative.dev/docs/getting-started), and
[CocoaPods](https://guides.cocoapods.org/using/getting-started.html).

## Usage

shadow-cljs and react-native must already be installed.

```shell
$ npx react-native-init-shadow MyProjectName
```

Options 

```shell
# Install a specific react-native version. Note that
# react-native-windows may not work with the latest RN.
$ npx react-native-init-shadow MyProjectName --version 0.62.2
```

```shell
# Generate a desktop (windows + macos) project, or a mobile (ios +
# android) project.
$ npx react-native-init-shadow MyProjectName --platform desktop|mobile
```

```shell
# Set the package name for Android. ApplicationId will be
# com.mycompany.myprojectname
$ npx react-native-init-shadow com.mycompany/MyProjectName
```

## License

MIT License

Copyright (c) 2020 Tom Goldsmith

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
