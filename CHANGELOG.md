# Changelog

All notable changes to this project are documented in this file.

The project follows Semantic Versioning while the public API is stabilized.

## Unreleased

- Remove the Edge-to-Edge API and demo.
- Correct optional dependency documentation and remove unused processors.
- Redesign the Glide image API with explicit shape, scale, placeholder, and error configuration.
- Remove the old `setImageUrl*` compatibility API; migrate to `loadImage` and `ImageLoadConfig`.
- Make all Glide utilities independent from `androidx.activity` and glide-transformations.
- Make `ImageLoadConfig` immutable, use independent placeholder/error/fallback resources, and apply thumbnail configuration consistently to Drawable and Bitmap requests.
- Add a runnable demo catalog covering every library area: custom views, images, text/Markwon, storage/screenshots, and sequential animation.
- Remove unrelated consumer shrinker rules.
- Improve screenshot saving, animation lifecycle handling, and public View APIs.
- Prefix XML attributes and enum resources with `avu_` to avoid resource merge conflicts.
- Add CI verification and an Apache-2.0 license.

## 0.0.14

- Remove ImageViewer and ImagePicker.
