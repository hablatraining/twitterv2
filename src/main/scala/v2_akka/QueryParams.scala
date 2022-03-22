package v2_akka

trait QueryParams{

    implicit class Params(params: Map[String, String]){
        def add(name: String, value: Option[String]): Map[String, String] = 
            value.fold(params){ v => params + ((name, v)) }
        def add(name: String, value: String): Map[String, String] = 
            add(name, Some(value))
    }
}