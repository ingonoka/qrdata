// Supposed to get rid of WARNING in asset size limit:
//                  The following asset(s) exceed the recommended size limit (244 KiB)
config.performance = {
        // This is the size limit in bytes for any individual asset file, such as
        // a JavaScript bundle, CSS file, or image.
        // Set the maximum asset size in bytes. For example, 500 KB (500000 bytes)
        maxAssetSize: 1000000,

        // This is the combined size limit for all assets that are part of
        // an initial entry point.
        // Set the maximum entrypoint size in bytes. For example, 700 KB (700000 bytes)
        maxEntrypointSize: 700000,

        // This determines how Webpack should react when the size limits are exceeded.
        // A value of warning will display a yellow warning message, while error will
        // show a red error and fail the build.
        // Display a warning message when limits are exceeded.
        // You can change 'warning' to 'error' to fail the build.
        hints: 'warning',
    };
