# json-resourcebundle

A [ResourceBundle.Control](https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.Control.html) reading JSON files.  
This can be used to share the same I18 files between frontend and backend.

### Setup
The control should be created once during application startup (i.e : a spring singleton bean):
```
JSONResourceBundleControl control = new JSONResourceBundleControl(resourceLoader, objectMapper, 10, stringify);
```
***Note : The spring ApplicationContext is a ResourceLoader and can be used directly.***   
A message can be access using the [ResourceBundle](https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.html) API.
```
ResourceBundle bundle = ResourceBundle.getBundle("a.b.TestBundle", control, Locale.FRANCE);
String message = bundle.getObject("messageKey"));
```
This will try to load the classpath resources :
* a.b.TestBundle_fr_FR.json
* a.b.TestBundle_fr.json 
* a.b.TestBundle.json

Where the bundle base name can be prefixed with [Spring resource loader](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/Resource.html) protocols classpath or file.
```
ResourceBundle bundle = ResourceBundle.getBundle("file:i18n/a/b/TestBundle", control);
String message = bundle.getObject("messageKey"));
```
This will try to load the files :
* a/b/TestBundle_fr_FR.json
* a/b/TestBundle_fr.json
* a/b/TestBundle.json

### JSON transform : 
The json file is flattened by joining keys with '.'.
```json
{
  "key1": "The value of key 1 from classpath",
  "key2": "The value of key 2 from classpath",
  "object": {
    "key1": "The value of key 1 from object",
    "key2": "The value of key 2 from object"
  },
  "array": [
    "The first value of array",
    "The second value of array"
  ]
}
```

Will produce the equivalent of this .properties :
```properties
key1=The value of key 1 from classpath
key2=The value of key 2 from classpath
object.key1=The value of key 1 from object
object.key2=The value of key 2 from object
array.0=The first value of array
array.1=The second value of array
```

By default, JSON number and boolean are not transformed to string and exposed to ResourceBundle unchanged, 
this can be prevented using the ``stringify`` parameter of ``JSONResourceBundleControl`` to convert any type to string.
