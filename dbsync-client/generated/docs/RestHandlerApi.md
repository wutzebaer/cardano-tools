# RestHandlerApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getAddressByHandle**](RestHandlerApi.md#getAddressByHandle) | **GET** /cardanoDbSyncApi/handles/{handle} | Get address for handle |
| [**getAddressTokenList**](RestHandlerApi.md#getAddressTokenList) | **GET** /cardanoDbSyncApi/{address}/token | getAddressTokenList |
| [**getEpochStake**](RestHandlerApi.md#getEpochStake) | **GET** /cardanoDbSyncApi/epochStake/{poolHash}/{epoch} | getEpochStake |
| [**getHandles**](RestHandlerApi.md#getHandles) | **GET** /cardanoDbSyncApi/{stakeAddress}/handles | Get all handles from a stakeAddress |
| [**getLastMint**](RestHandlerApi.md#getLastMint) | **POST** /cardanoDbSyncApi/lastMint | Get last minted tokens for stakeAddress and policy ids |
| [**getMinswapPools**](RestHandlerApi.md#getMinswapPools) | **GET** /cardanoDbSyncApi/minswap/{policyId}/{assetName} | Get minswap pools for token |
| [**getOwners**](RestHandlerApi.md#getOwners) | **GET** /cardanoDbSyncApi/policy/{policyId}/owners | Get all token owners of a policyId, values get updated twice a day |
| [**getPoolList**](RestHandlerApi.md#getPoolList) | **GET** /cardanoDbSyncApi/poolList | getPoolList |
| [**getReturnAddress**](RestHandlerApi.md#getReturnAddress) | **GET** /cardanoDbSyncApi/{stakeAddress}/returnAddress | Find the first known address with the same stake address, which should not be mangled |
| [**getStakeAddress**](RestHandlerApi.md#getStakeAddress) | **GET** /cardanoDbSyncApi/{address}/stakeAddress | Find stakeAddress of address |
| [**getStakeAddressByHash**](RestHandlerApi.md#getStakeAddressByHash) | **GET** /cardanoDbSyncApi/stakeAddress/{stakeAddressHash} | Find stakeAddress by stakeAddressHash |
| [**getStakeHashByAddress**](RestHandlerApi.md#getStakeHashByAddress) | **GET** /cardanoDbSyncApi/stakeHash/{stakeAddress} | Find stakeAddressHash by stakeAddress |
| [**getStakeInfo**](RestHandlerApi.md#getStakeInfo) | **GET** /cardanoDbSyncApi/{stakeAddress}/stakeInfo | Get infos where address is staked to |
| [**getStatement**](RestHandlerApi.md#getStatement) | **GET** /cardanoDbSyncApi/{address}/statement | Get all transactions for an address or stakeAddress |
| [**getTip**](RestHandlerApi.md#getTip) | **GET** /cardanoDbSyncApi/tip | Returns current tip of db |
| [**getTokenDetails**](RestHandlerApi.md#getTokenDetails) | **GET** /cardanoDbSyncApi/token/{policyId}/{assetName} | getTokenDetails |
| [**getTokenList**](RestHandlerApi.md#getTokenList) | **GET** /cardanoDbSyncApi/token | getTokenList |
| [**getUtxos**](RestHandlerApi.md#getUtxos) | **GET** /cardanoDbSyncApi/{address}/utxos | Find utxos of given address or stakeAddress including multi assets |
| [**isTransactionConfirmed**](RestHandlerApi.md#isTransactionConfirmed) | **GET** /cardanoDbSyncApi/transaction/{txId}/confirmed | Checks is a txid has been included in the chain |



## getAddressByHandle

> StakeAddress getAddressByHandle(handle)

Get address for handle

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
        String handle = "petergrossmann"; // String | 
        try {
            StakeAddress result = apiInstance.getAddressByHandle(handle);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getAddressByHandle");
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
| **handle** | **String**|  | |

### Return type

[**StakeAddress**](StakeAddress.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


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
- **Accept**: application/json


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
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getHandles

> List&lt;StakeAddress&gt; getHandles(stakeAddress)

Get all handles from a stakeAddress

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
            List<StakeAddress> result = apiInstance.getHandles(stakeAddress);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getHandles");
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

[**List&lt;StakeAddress&gt;**](StakeAddress.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getLastMint

> List&lt;TokenDetails&gt; getLastMint(getLastMintRequest)

Get last minted tokens for stakeAddress and policy ids

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
        GetLastMintRequest getLastMintRequest = new GetLastMintRequest(); // GetLastMintRequest | 
        try {
            List<TokenDetails> result = apiInstance.getLastMint(getLastMintRequest);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getLastMint");
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
| **getLastMintRequest** | [**GetLastMintRequest**](GetLastMintRequest.md)|  | |

### Return type

[**List&lt;TokenDetails&gt;**](TokenDetails.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getMinswapPools

> List&lt;LiquidityPool&gt; getMinswapPools(policyId, assetName)

Get minswap pools for token

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
        String policyId = "89267e9a35153a419e1b8ffa23e511ac39ea4e3b00452e9d500f2982"; // String | 
        String assetName = "436176616c6965724b696e67436861726c6573"; // String | 
        try {
            List<LiquidityPool> result = apiInstance.getMinswapPools(policyId, assetName);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getMinswapPools");
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

[**List&lt;LiquidityPool&gt;**](LiquidityPool.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getOwners

> List&lt;OwnerInfo&gt; getOwners(policyId)

Get all token owners of a policyId, values get updated twice a day

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
        String policyId = "89267e9a35153a419e1b8ffa23e511ac39ea4e3b00452e9d500f2982"; // String | 
        try {
            List<OwnerInfo> result = apiInstance.getOwners(policyId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getOwners");
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

### Return type

[**List&lt;OwnerInfo&gt;**](OwnerInfo.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


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
- **Accept**: application/json


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
- **Accept**: application/json


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
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getStakeAddressByHash

> StakeAddress getStakeAddressByHash(stakeAddressHash)

Find stakeAddress by stakeAddressHash

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
        String stakeAddressHash = "e1ddbe7a587e6bdd2674bf53fc093226bbd43af035f4ea07d781167966"; // String | 
        try {
            StakeAddress result = apiInstance.getStakeAddressByHash(stakeAddressHash);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getStakeAddressByHash");
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
| **stakeAddressHash** | **String**|  | |

### Return type

[**StakeAddress**](StakeAddress.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getStakeHashByAddress

> StakeAddress getStakeHashByAddress(stakeAddress)

Find stakeAddressHash by stakeAddress

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
            StakeAddress result = apiInstance.getStakeHashByAddress(stakeAddress);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getStakeHashByAddress");
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

[**StakeAddress**](StakeAddress.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


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
- **Accept**: application/json


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
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## getTip

> Long getTip()

Returns current tip of db

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
            Long result = apiInstance.getTip();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#getTip");
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

**Long**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


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
        String policyId = "89267e9a35153a419e1b8ffa23e511ac39ea4e3b00452e9d500f2982"; // String | 
        String assetName = "436176616c6965724b696e67436861726c6573"; // String | 
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
- **Accept**: application/json


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
        String filter = "89267e9a35153a419e1b8ffa23e511ac39ea4e3b00452e9d500f2982"; // String | 
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
- **Accept**: application/json


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
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |


## isTransactionConfirmed

> Boolean isTransactionConfirmed(txId)

Checks is a txid has been included in the chain

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
        String txId = "a6ca444bd39cb51c7e997a9cead4a8071e2f7e5d1579ac4194b6aaaba923bc58"; // String | 
        try {
            Boolean result = apiInstance.isTransactionConfirmed(txId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling RestHandlerApi#isTransactionConfirmed");
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
| **txId** | **String**|  | |

### Return type

**Boolean**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |

