# RestHandlerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAddressTokenList**](RestHandlerApi.md#getAddressTokenList) | **GET** /cardanoDbSyncApi/{address}/token | getAddressTokenList |
| [**getEpochStake**](RestHandlerApi.md#getEpochStake) | **GET** /cardanoDbSyncApi/epochStake/{poolHash}/{epoch} | getEpochStake |
| [**getPoolList**](RestHandlerApi.md#getPoolList) | **GET** /cardanoDbSyncApi/poolList | getPoolList |
| [**getReturnAddress**](RestHandlerApi.md#getReturnAddress) | **GET** /cardanoDbSyncApi/{stakeAddress}/returnAddress | Find the first known address with the same stake address, which should not be mangled |
| [**getStakeAddress**](RestHandlerApi.md#getStakeAddress) | **GET** /cardanoDbSyncApi/{address}/stakeAddress | Find stakeAddress of address |
| [**getStakeInfo**](RestHandlerApi.md#getStakeInfo) | **GET** /cardanoDbSyncApi/{stakeAddress}/stakeInfo | Get infos where address is staked to |
| [**getStatement**](RestHandlerApi.md#getStatement) | **GET** /cardanoDbSyncApi/{address}/statement | Get all transactions for an address or stakeAddress |
| [**getTokenDetails**](RestHandlerApi.md#getTokenDetails) | **GET** /cardanoDbSyncApi/token/{policyId}/{assetName} | getTokenDetails |
| [**getTokenList**](RestHandlerApi.md#getTokenList) | **GET** /cardanoDbSyncApi/token | getTokenList |
| [**getUtxos**](RestHandlerApi.md#getUtxos) | **GET** /cardanoDbSyncApi/{address}/utxos | Find utxos of given address or stakeAddress including multi assets |



## getAddressTokenList

> List&lt;TokenListItem&gt; getAddressTokenList(address)

getAddressTokenList

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
        String address = "stake1u8wmu7jc0e4a6fn5haflczfjy6aagwhsxh6w5p7hsyt8jeshhy0rn"; // String | 
        try {
            List<TokenListItem> result = apiInstance.getAddressTokenList(address);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getAddressTokenList");
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


## getEpochStake

> List&lt;EpochStake&gt; getEpochStake(poolHash, epoch)

getEpochStake

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
        String poolHash = "pool180fejev4xgwe2y53ky0pxvgxr3wcvkweu6feq5mdljfzcsmtg6u"; // String | 
        Integer epoch = 432; // Integer | 
        try {
            List<EpochStake> result = apiInstance.getEpochStake(poolHash, epoch);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getEpochStake");
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
| **poolHash** | **String**|  | |
| **epoch** | **Integer**|  | |

### Return type

[**List&lt;EpochStake&gt;**](EpochStake.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


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

> ReturnAddress getReturnAddress(stakeAddress)

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
        String stakeAddress = "stake1u8wmu7jc0e4a6fn5haflczfjy6aagwhsxh6w5p7hsyt8jeshhy0rn"; // String | 
        try {
            ReturnAddress result = apiInstance.getReturnAddress(stakeAddress);
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
| **stakeAddress** | **String**|  | |

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


## getStakeAddress

> StakeAddress getStakeAddress(address)

Find stakeAddress of address

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
            StakeAddress result = apiInstance.getStakeAddress(address);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getStakeAddress");
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

[**StakeAddress**](StakeAddress.md)

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

> StakeInfo getStakeInfo(stakeAddress)

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
        String stakeAddress = "stake1u8wmu7jc0e4a6fn5haflczfjy6aagwhsxh6w5p7hsyt8jeshhy0rn"; // String | 
        try {
            StakeInfo result = apiInstance.getStakeInfo(stakeAddress);
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
| **stakeAddress** | **String**|  | |

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


## getStatement

> List&lt;AccountStatementRow&gt; getStatement(address)

Get all transactions for an address or stakeAddress

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
        String address = "stake1u8wmu7jc0e4a6fn5haflczfjy6aagwhsxh6w5p7hsyt8jeshhy0rn"; // String | 
        try {
            List<AccountStatementRow> result = apiInstance.getStatement(address);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getStatement");
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

[**List&lt;AccountStatementRow&gt;**](AccountStatementRow.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTokenDetails

> TokenDetails getTokenDetails(policyId, assetName)

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
            TokenDetails result = apiInstance.getTokenDetails(policyId, assetName);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getTokenDetails");
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
        String filter = "d1edc4dfb4f5f7fb240239ad64a4730c2fd4744eda3c8a7d0fff1f92"; // String | 
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


## getUtxos

> List&lt;Utxo&gt; getUtxos(address)

Find utxos of given address or stakeAddress including multi assets

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
        String address = "stake1u8wmu7jc0e4a6fn5haflczfjy6aagwhsxh6w5p7hsyt8jeshhy0rn"; // String | 
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

