# Rollplayer
*now on Java!*

## Setup
- Compile the project.
- Create a rollplayer.properties file in the same folder as the jar with dependencies.
  - The only field currently is `discord.token`, which should be set to the token.
- Run the jar file.

## Licensing
Most of the files are GPLv3 licensed. There are, however, some exceptions. 

These will be shown on the top of the file with a block comment; e.g. from `rollplayerlib3.StringReader`:

```java
package dev.infernity.rollplayer.rollplayerlib3;

import dev.infernity.rollplayer.rollplayerlib3.exceptions.SyntaxException;

/*

This code is taken from the Brigadier codebase and is thusly licensed under the MIT License:

Copyright (c) Microsoft Corporation. All rights reserved.

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

*/

public class StringReader { /* code removed */ }
```

If the notice is not there, you should assume it is licensed under the GPLv3.
