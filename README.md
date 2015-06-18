# Boot + Middleman

Builds a middleman project from a subdirectory. By default, assumes the
`assets` folder, but this can be configured.

## Setup

In your `build.boot`:

``` clojure
(set-env!
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.6.0" :scope "provided"]
                 [com.joshuadavey/boot-middleman "0.0.4" :scope "test"]])

(require '[com.joshuadavey.boot-middleman :refer [middleman]])
```

## Usage

```
boot middleman
```

Or, for continuous builds:

```
boot watch middleman
```

For more options, see help with `boot middleman -h`.
