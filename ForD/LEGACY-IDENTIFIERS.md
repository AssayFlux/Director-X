# Legacy identifiers retained for compatibility

The following inherited identifiers are not ordinary display branding. They are tied to Android or persisted-data compatibility and must not be renamed as part of a cosmetic branding sweep.

| Identifier family | Current examples | Why retained |
|---|---|---|
| Android application ID / namespace | `com.novacut.editor` | Changing it creates a different Android app identity and prevents ordinary in-place updates. |
| Kotlin source package paths | `com/novacut/editor/**` | Must stay aligned with namespace, imports, manifest references, tests, and generated code. |
| Intent actions / shortcuts | `com.novacut.editor.action.*` | External shortcuts and app integrations may refer to these exact action strings. |
| Archive extensions | `.clearcut`, `.clearcut-template` | Existing user project files and import associations depend on them. |
| MIME / schema identifiers | `com.clearcut.*`, `novaCut*` | Interchange formats and serialized project/plugin metadata may persist these values. |
| Database / file names | `clearcut.db` and related legacy filenames | Existing app-private data and migration code may depend on them. |
| Signing identity | pinned upstream release certificate | Required for updating an existing installed app; replacing it strands existing installs. |
| Upstream release / source references | upstream repository URLs in provenance and notices | Needed to identify the inherited source and preserve provenance. |

A future technical identity migration must be versioned, include explicit export/import or database migration paths, preserve backwards-compatible readers, update all manifest/provider/shortcut/test references, and pass upgrade tests on a device. Until then, retain these identifiers and use Director X for all user-facing product surfaces.
