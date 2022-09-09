# Markdown to Patchouli

*A Gradle plugin with an interesting name*

## Basic Usage

This plugin is published as a Github Package to this repository.
Simply add `https://maven.pkg.github.com/ssblur/markdown-to-patchouli`
to your project's `repositories` and add the plugin to your `plugins` with
`id "com.ssblur.mtp"`, optionally with a version specifier.

You can configure this plugin using the `mtp` Object. 
Usage (with default values) is detailed below.

```kts
mtp {
    namespace "minecraft"
    bookId "guide"
    input "documentation"
    output "generated/src/main/resources"
}
```

`namespace` is the namespace to put resources and data into when 
generating output files. If this is part of a mod, this should be 
your mod ID. This is `_YOURMODID_` as shown 
[here](https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/getting-started#1-locate-your-patchouli_books-directory).

`bookId` is the ID used for your book. This is the name chosen
[here](https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/getting-started#2-create-your-folder-structure).

`input` is the directory (relative to your project directory) in which
your Markdown files are located. By default, this is `documentation`.

`output` is the directory (relative to your project director) to which 
data and resources will be output. 
By default, this is `generated/src/main/resources`, as a (hopefully)
reasonable default for multiloader projects.

## Expectations and Behavior

This mod expects a `README.md` file in any directories which will be used
as categories, and a root `README.md` in the `input` directory, which will
be used to generate the book's landing text and title.

### Metadata

When selecting entry and book titles, the first title which appears in the 
corresponding article will be used.

#### Tags

Support for tags within HTML comments (`<!--- --->`) is planned to allow 
specifying other metadata such as icons, book texture, model, etc.
This is not yet implemented.

### Pages

If an image is encountered, a new page will be started, using the previous
title if no other text has been encountered yet.

Pages will automatically be broken after ~500 non-zero-width characters, 
and will attempt to break on whitespace or zero-width breaks.
In the future, this will include valid formatting tags which do not 
break lines or insert characters.

## Examples

For an example project, you can check out 
[Redderstone](https://github.com/ssblur/redderstone).
This project uses MTP to generate in-game documentation from Markdown files
located in the documentation directory.

If you have a project which uses MTP, and would like it to be featured here,
feel free to either open a PR or shoot an email to 
[info@ssblur.com](mailto:info@ssblur.com).

## Contributing

If there's a feature missing which you think should be included in this 
project, either open an Issue or PR, or email
[info@ssblur.com](mailto:info@ssblur.com).
with a suggestion.