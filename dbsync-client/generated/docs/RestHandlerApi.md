# RestHandlerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getPoolList**](RestHandlerApi.md#getPoolList) | **GET** /cardanoDbSyncApi/poolList | getPoolList |
| [**getReturnAddress**](RestHandlerApi.md#getReturnAddress) | **GET** /cardanoDbSyncApi/{address}/returnAddress | Find the first known address with the same stake address, which should not be mangled |
| [**getStakeInfo**](RestHandlerApi.md#getStakeInfo) | **GET** /cardanoDbSyncApi/{address}/stakeInfo | Get infos where address is staked to |
| [**getTokenList**](RestHandlerApi.md#getTokenList) | **GET** /cardanoDbSyncApi/token | getTokenList |
| [**getTokenList1**](RestHandlerApi.md#getTokenList1) | **GET** /cardanoDbSyncApi/token/{policyId}/{assetName} | getTokenDetails |
| [**getUtxos**](RestHandlerApi.md#getUtxos) | **GET** /cardanoDbSyncApi/{address}/utxos | Find utxos of given address including multi assets |



## getPoolList

> List&lt;PoolInfo&gt; getPoolList()

getPoolList

### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        RestHandlerApi apiInstance = new RestHandlerApi(defaultClient);
        try {
            List<PoolInfo> result = apiInstance.getPoolList();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getPoolList");
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

[**List&lt;PoolInfo&gt;**](PoolInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getReturnAddress

> ReturnAddress getReturnAddress(address)

Find the first known address with the same stake address, which should not be mangled

### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        RestHandlerApi apiInstance = new RestHandlerApi(defaultClient);
        String address = "stake1uywupacwlt7vdpgj26vmtpy3fx5d3tv6v4zrdppaxkmls5sak6xqg"; // String | 
        try {
            ReturnAddress result = apiInstance.getReturnAddress(address);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getReturnAddress");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **address** | **String**|  | |

### Return type

[**ReturnAddress**](ReturnAddress.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getStakeInfo

> StakeInfo getStakeInfo(address)

Get infos where address is staked to

### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        RestHandlerApi apiInstance = new RestHandlerApi(defaultClient);
        String address = "addr1qx8lsj4menq5s7w5f8jupm64n9d3aamvcppllujwse473636fhhttcg3x8kfhm6qqpvujfhgmu8jww3mfn49m3fkjssqhx0348"; // String | 
        try {
            StakeInfo result = apiInstance.getStakeInfo(address);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getStakeInfo");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **address** | **String**|  | |

### Return type

[**StakeInfo**](StakeInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTokenList

> List&lt;TokenListItem&gt; getTokenList(afterMintid, beforeMintid, filter)

getTokenList

### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        RestHandlerApi apiInstance = new RestHandlerApi(defaultClient);
        Long afterMintid = 56L; // Long | 
        Long beforeMintid = 56L; // Long | 
        String filter = "filter_example"; // String | 
        try {
            List<TokenListItem> result = apiInstance.getTokenList(afterMintid, beforeMintid, filter);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getTokenList");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **afterMintid** | **Long**|  | [optional] |
| **beforeMintid** | **Long**|  | [optional] |
| **filter** | **String**|  | [optional] |

### Return type

[**List&lt;TokenListItem&gt;**](TokenListItem.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTokenList1

> TokenDetails getTokenList1(policyId, assetName)

getTokenDetails

### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        RestHandlerApi apiInstance = new RestHandlerApi(defaultClient);
        String policyId = "d1edc4dfb4f5f7fb240239ad64a4730c2fd4744eda3c8a7d0fff1f92"; // String | 
        String assetName = "504f524b5958383835"; // String | 
        try {
            TokenDetails result = apiInstance.getTokenList1(policyId, assetName);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getTokenList1");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **policyId** | **String**|  | |
| **assetName** | **String**|  | |

### Return type

[**TokenDetails**](TokenDetails.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getUtxos

> List&lt;Utxo&gt; getUtxos(address)

Find utxos of given address including multi assets

### Example

```java
// Import classes:
import de.peterspace.cardanodbsyncapi.client.ApiClient;
import de.peterspace.cardanodbsyncapi.client.ApiException;
import de.peterspace.cardanodbsyncapi.client.Configuration;
import de.peterspace.cardanodbsyncapi.client.models.*;
import de.peterspace.cardanodbsyncapi.client.RestHandlerApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost:8080");

        RestHandlerApi apiInstance = new RestHandlerApi(defaultClient);
        String address = "addr1qx8lsj4menq5s7w5f8jupm64n9d3aamvcppllujwse473636fhhttcg3x8kfhm6qqpvujfhgmu8jww3mfn49m3fkjssqhx0348"; // String | 
        try {
            List<Utxo> result = apiInstance.getUtxos(address);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getUtxos");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **address** | **String**|  | |

### Return type

[**List&lt;Utxo&gt;**](Utxo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

