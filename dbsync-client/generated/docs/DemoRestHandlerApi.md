# DemoRestHandlerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getCached**](DemoRestHandlerApi.md#getCached) | **GET** /cardanoDbSyncApi/demo/cached |  |
| [**getRated**](DemoRestHandlerApi.md#getRated) | **GET** /cardanoDbSyncApi/demo/rated |  |
| [**getTest**](DemoRestHandlerApi.md#getTest) | **GET** /cardanoDbSyncApi/demo/test |  |



## getCached

> String getCached()



### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.DemoRestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        DemoRestHandlerApi apiInstance = new DemoRestHandlerApi(defaultClient);
        try {
            String result = apiInstance.getCached();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemoRestHandlerApi#getCached");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getRated

> String getRated()



### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.DemoRestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        DemoRestHandlerApi apiInstance = new DemoRestHandlerApi(defaultClient);
        try {
            String result = apiInstance.getRated();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemoRestHandlerApi#getRated");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTest

> String getTest()



### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.DemoRestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        DemoRestHandlerApi apiInstance = new DemoRestHandlerApi(defaultClient);
        try {
            String result = apiInstance.getTest();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DemoRestHandlerApi#getTest");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

