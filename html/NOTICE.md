# Third-party source vendored into `:html`

This module bundles source files from two third-party projects to provide a
pure-Java Box2D implementation that the TeaVM backend can compile to
JavaScript. CaveDroid itself is MIT (see top-level `LICENSE`); the files
listed below retain their original licenses and per-file copyright headers.
No header has been modified.

## libGDX — `gdx-box2d-gwt`

- Origin: <https://github.com/libgdx/libgdx>, path
  `extensions/gdx-box2d/gdx-box2d-gwt/src/com/badlogic/gdx/physics/box2d/gwt/emu/`
- License: Apache License 2.0
- Files in this module: 49 files under
  `src/main/java/emu/com/badlogic/gdx/physics/box2d/**`
- Role: pure-Java reimplementation of the `com.badlogic.gdx.physics.box2d.*`
  API, delegating to jbox2d.

Full Apache 2.0 license text: <http://www.apache.org/licenses/LICENSE-2.0>

## jbox2d — Daniel Murphy

- Origin: vendored alongside libGDX's `gdx-box2d-gwt` (see above); upstream is
  <https://github.com/jbox2d/jbox2d>.
- License: BSD 2-Clause (Simplified BSD)
- Files in this module: 118 files under `src/main/java/emu/org/jbox2d/**` plus
  `src/main/java/emu/java/lang/StrictMath.java`
- Role: pure-Java port of the Box2D 2.x physics engine; backs the libGDX
  wrappers above.

Full license text appears verbatim in the header of every vendored file. A
representative copy:

```
Copyright (c) 2013, Daniel Murphy
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
```

## TeaVM emulation stubs

The hand-written stubs under `src/main/java/emu/java/util/concurrent/**` and
`src/main/java/emulate/java/lang/**` were authored for this project and are
covered by CaveDroid's top-level MIT license.
