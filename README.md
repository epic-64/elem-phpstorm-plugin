# Elem PHPStorm Plugin

A PHPStorm plugin that provides CSS class completion and navigation for the [Elem](https://github.com/epic-64/elem) PHP templating library.

## Features

- **CSS Class Completion**: Get autocomplete suggestions for CSS classes when typing in `class:` parameters of Elem functions
- **Navigation**: Ctrl+Click on CSS class names to jump to their definitions in CSS files
- **Multi-class Support**: Works with space-separated class names like `'btn btn-primary'`

## Supported Functions

The plugin recognizes all Elem helper functions:
- `div()`, `span()`, `p()`, `a()`, `h()`
- `img()`, `button()`, `input()`, `form()`, `label()`
- `ul()`, `ol()`, `li()`
- `table()`, `tr()`, `td()`, `th()`
- `el()`, `nav()`, `header()`, `footer()`, `section()`, `article()`
- And more...

## Usage

Once installed, the plugin automatically activates when you use Elem functions with the `class:` named parameter:

```php
div(class: 'container')(        // <- Ctrl+Click 'container' to navigate
    h(1, class: 'title'),       // <- CSS completion available here
    p(class: 'intro-text')
)
```

## Building

```bash
./gradlew buildPlugin
```

The plugin ZIP will be created in `build/distributions/`.

## Installing

1. Build the plugin or download from releases
2. In PHPStorm: **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the plugin ZIP file
4. Restart PHPStorm

## Development

### Requirements
- JDK 21+
- Gradle 8.11+

### Running in Development
```bash
./gradlew runIde
```

This will launch a sandboxed PHPStorm instance with the plugin installed.

## License

MIT License
