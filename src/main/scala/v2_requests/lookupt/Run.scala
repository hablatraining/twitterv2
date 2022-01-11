package v2_requests.lookupt

object Run extends HttpEndpoint[Request]
    with From 
    with To{
        type Response = v2.lookupt.Response
    }