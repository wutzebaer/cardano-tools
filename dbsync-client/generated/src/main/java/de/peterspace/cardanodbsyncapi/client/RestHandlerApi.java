package de.peterspace.cardanodbsyncapi.client;

import de.peterspace.cardanodbsyncapi.client.ApiClient;

import de.peterspace.cardanodbsyncapi.client.model.AccountStatementRow;
import de.peterspace.cardanodbsyncapi.client.model.EpochStake;
import de.peterspace.cardanodbsyncapi.client.model.GetLastMintRequest;
import de.peterspace.cardanodbsyncapi.client.model.LiquidityPool;
import de.peterspace.cardanodbsyncapi.client.model.OwnerInfo;
import de.peterspace.cardanodbsyncapi.client.model.PoolInfo;
import de.peterspace.cardanodbsyncapi.client.model.ReturnAddress;
import de.peterspace.cardanodbsyncapi.client.model.StakeAddress;
import de.peterspace.cardanodbsyncapi.client.model.StakeInfo;
import de.peterspace.cardanodbsyncapi.client.model.TokenDetails;
import de.peterspace.cardanodbsyncapi.client.model.TokenListItem;
import de.peterspace.cardanodbsyncapi.client.model.TxOut;
import de.peterspace.cardanodbsyncapi.client.model.Utxo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-03-06T20:43:49.743375300+01:00[Europe/Berlin]")
public class RestHandlerApi {
    private ApiClient apiClient;

    public RestHandlerApi() {
        this(new ApiClient());
    }

    public RestHandlerApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get address for handle
     * 
     * <p><b>200</b> - OK
     * @param handle  (required)
     * @return StakeAddress
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StakeAddress getAddressByHandle(String handle) throws RestClientException {
        return getAddressByHandleWithHttpInfo(handle).getBody();
    }

    /**
     * Get address for handle
     * 
     * <p><b>200</b> - OK
     * @param handle  (required)
     * @return ResponseEntity&lt;StakeAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StakeAddress> getAddressByHandleWithHttpInfo(String handle) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'handle' is set
        if (handle == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'handle' when calling getAddressByHandle");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("handle", handle);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<StakeAddress> localReturnType = new ParameterizedTypeReference<StakeAddress>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/handles/{handle}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * getAddressTokenList
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return List&lt;TokenListItem&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TokenListItem> getAddressTokenList(String address) throws RestClientException {
        return getAddressTokenListWithHttpInfo(address).getBody();
    }

    /**
     * getAddressTokenList
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return ResponseEntity&lt;List&lt;TokenListItem&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<TokenListItem>> getAddressTokenListWithHttpInfo(String address) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'address' is set
        if (address == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'address' when calling getAddressTokenList");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("address", address);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<TokenListItem>> localReturnType = new ParameterizedTypeReference<List<TokenListItem>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/token", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * getEpochStake
     * 
     * <p><b>200</b> - OK
     * @param poolHash  (required)
     * @param epoch  (required)
     * @return List&lt;EpochStake&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<EpochStake> getEpochStake(String poolHash, Integer epoch) throws RestClientException {
        return getEpochStakeWithHttpInfo(poolHash, epoch).getBody();
    }

    /**
     * getEpochStake
     * 
     * <p><b>200</b> - OK
     * @param poolHash  (required)
     * @param epoch  (required)
     * @return ResponseEntity&lt;List&lt;EpochStake&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<EpochStake>> getEpochStakeWithHttpInfo(String poolHash, Integer epoch) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'poolHash' is set
        if (poolHash == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'poolHash' when calling getEpochStake");
        }
        
        // verify the required parameter 'epoch' is set
        if (epoch == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'epoch' when calling getEpochStake");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("poolHash", poolHash);
        uriVariables.put("epoch", epoch);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<EpochStake>> localReturnType = new ParameterizedTypeReference<List<EpochStake>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/epochStake/{poolHash}/{epoch}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get all handles from a stakeAddress
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return List&lt;StakeAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<StakeAddress> getHandles(String stakeAddress) throws RestClientException {
        return getHandlesWithHttpInfo(stakeAddress).getBody();
    }

    /**
     * Get all handles from a stakeAddress
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return ResponseEntity&lt;List&lt;StakeAddress&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<StakeAddress>> getHandlesWithHttpInfo(String stakeAddress) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'stakeAddress' is set
        if (stakeAddress == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'stakeAddress' when calling getHandles");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("stakeAddress", stakeAddress);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<StakeAddress>> localReturnType = new ParameterizedTypeReference<List<StakeAddress>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{stakeAddress}/handles", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get last minted tokens for stakeAddress and policy ids
     * 
     * <p><b>200</b> - OK
     * @param getLastMintRequest  (required)
     * @return List&lt;TokenDetails&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TokenDetails> getLastMint(GetLastMintRequest getLastMintRequest) throws RestClientException {
        return getLastMintWithHttpInfo(getLastMintRequest).getBody();
    }

    /**
     * Get last minted tokens for stakeAddress and policy ids
     * 
     * <p><b>200</b> - OK
     * @param getLastMintRequest  (required)
     * @return ResponseEntity&lt;List&lt;TokenDetails&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<TokenDetails>> getLastMintWithHttpInfo(GetLastMintRequest getLastMintRequest) throws RestClientException {
        Object localVarPostBody = getLastMintRequest;
        
        // verify the required parameter 'getLastMintRequest' is set
        if (getLastMintRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'getLastMintRequest' when calling getLastMint");
        }
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<TokenDetails>> localReturnType = new ParameterizedTypeReference<List<TokenDetails>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/lastMint", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get minswap pools for token
     * 
     * <p><b>200</b> - OK
     * @param policyId  (required)
     * @param assetName  (required)
     * @return List&lt;LiquidityPool&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<LiquidityPool> getMinswapPools(String policyId, String assetName) throws RestClientException {
        return getMinswapPoolsWithHttpInfo(policyId, assetName).getBody();
    }

    /**
     * Get minswap pools for token
     * 
     * <p><b>200</b> - OK
     * @param policyId  (required)
     * @param assetName  (required)
     * @return ResponseEntity&lt;List&lt;LiquidityPool&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<LiquidityPool>> getMinswapPoolsWithHttpInfo(String policyId, String assetName) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'policyId' is set
        if (policyId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'policyId' when calling getMinswapPools");
        }
        
        // verify the required parameter 'assetName' is set
        if (assetName == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'assetName' when calling getMinswapPools");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("policyId", policyId);
        uriVariables.put("assetName", assetName);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<LiquidityPool>> localReturnType = new ParameterizedTypeReference<List<LiquidityPool>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/minswap/{policyId}/{assetName}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get all token owners of a policyId, values get updated twice a day
     * 
     * <p><b>200</b> - OK
     * @param policyId  (required)
     * @return List&lt;OwnerInfo&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<OwnerInfo> getOwners(String policyId) throws RestClientException {
        return getOwnersWithHttpInfo(policyId).getBody();
    }

    /**
     * Get all token owners of a policyId, values get updated twice a day
     * 
     * <p><b>200</b> - OK
     * @param policyId  (required)
     * @return ResponseEntity&lt;List&lt;OwnerInfo&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<OwnerInfo>> getOwnersWithHttpInfo(String policyId) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'policyId' is set
        if (policyId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'policyId' when calling getOwners");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("policyId", policyId);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<OwnerInfo>> localReturnType = new ParameterizedTypeReference<List<OwnerInfo>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/policy/{policyId}/owners", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * getPoolList
     * 
     * <p><b>200</b> - OK
     * @return List&lt;PoolInfo&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<PoolInfo> getPoolList() throws RestClientException {
        return getPoolListWithHttpInfo().getBody();
    }

    /**
     * getPoolList
     * 
     * <p><b>200</b> - OK
     * @return ResponseEntity&lt;List&lt;PoolInfo&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<PoolInfo>> getPoolListWithHttpInfo() throws RestClientException {
        Object localVarPostBody = null;
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<PoolInfo>> localReturnType = new ParameterizedTypeReference<List<PoolInfo>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/poolList", HttpMethod.GET, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Find the first known address with the same stake address, which should not be mangled
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return ReturnAddress
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ReturnAddress getReturnAddress(String stakeAddress) throws RestClientException {
        return getReturnAddressWithHttpInfo(stakeAddress).getBody();
    }

    /**
     * Find the first known address with the same stake address, which should not be mangled
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return ResponseEntity&lt;ReturnAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<ReturnAddress> getReturnAddressWithHttpInfo(String stakeAddress) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'stakeAddress' is set
        if (stakeAddress == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'stakeAddress' when calling getReturnAddress");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("stakeAddress", stakeAddress);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<ReturnAddress> localReturnType = new ParameterizedTypeReference<ReturnAddress>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{stakeAddress}/returnAddress", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Find stakeAddress of address
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return StakeAddress
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StakeAddress getStakeAddress(String address) throws RestClientException {
        return getStakeAddressWithHttpInfo(address).getBody();
    }

    /**
     * Find stakeAddress of address
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return ResponseEntity&lt;StakeAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StakeAddress> getStakeAddressWithHttpInfo(String address) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'address' is set
        if (address == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'address' when calling getStakeAddress");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("address", address);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<StakeAddress> localReturnType = new ParameterizedTypeReference<StakeAddress>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/stakeAddress", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Find stakeAddress by stakeAddressHash
     * 
     * <p><b>200</b> - OK
     * @param stakeAddressHash  (required)
     * @return StakeAddress
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StakeAddress getStakeAddressByHash(String stakeAddressHash) throws RestClientException {
        return getStakeAddressByHashWithHttpInfo(stakeAddressHash).getBody();
    }

    /**
     * Find stakeAddress by stakeAddressHash
     * 
     * <p><b>200</b> - OK
     * @param stakeAddressHash  (required)
     * @return ResponseEntity&lt;StakeAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StakeAddress> getStakeAddressByHashWithHttpInfo(String stakeAddressHash) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'stakeAddressHash' is set
        if (stakeAddressHash == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'stakeAddressHash' when calling getStakeAddressByHash");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("stakeAddressHash", stakeAddressHash);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<StakeAddress> localReturnType = new ParameterizedTypeReference<StakeAddress>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/stakeAddress/{stakeAddressHash}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Find stakeAddressHash by stakeAddress
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return StakeAddress
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StakeAddress getStakeHashByAddress(String stakeAddress) throws RestClientException {
        return getStakeHashByAddressWithHttpInfo(stakeAddress).getBody();
    }

    /**
     * Find stakeAddressHash by stakeAddress
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return ResponseEntity&lt;StakeAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StakeAddress> getStakeHashByAddressWithHttpInfo(String stakeAddress) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'stakeAddress' is set
        if (stakeAddress == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'stakeAddress' when calling getStakeHashByAddress");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("stakeAddress", stakeAddress);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<StakeAddress> localReturnType = new ParameterizedTypeReference<StakeAddress>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/stakeHash/{stakeAddress}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get infos where address is staked to
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return StakeInfo
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StakeInfo getStakeInfo(String stakeAddress) throws RestClientException {
        return getStakeInfoWithHttpInfo(stakeAddress).getBody();
    }

    /**
     * Get infos where address is staked to
     * 
     * <p><b>200</b> - OK
     * @param stakeAddress  (required)
     * @return ResponseEntity&lt;StakeInfo&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StakeInfo> getStakeInfoWithHttpInfo(String stakeAddress) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'stakeAddress' is set
        if (stakeAddress == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'stakeAddress' when calling getStakeInfo");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("stakeAddress", stakeAddress);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<StakeInfo> localReturnType = new ParameterizedTypeReference<StakeInfo>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{stakeAddress}/stakeInfo", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get all transactions for an address or stakeAddress
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return List&lt;AccountStatementRow&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<AccountStatementRow> getStatement(String address) throws RestClientException {
        return getStatementWithHttpInfo(address).getBody();
    }

    /**
     * Get all transactions for an address or stakeAddress
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return ResponseEntity&lt;List&lt;AccountStatementRow&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<AccountStatementRow>> getStatementWithHttpInfo(String address) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'address' is set
        if (address == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'address' when calling getStatement");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("address", address);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<AccountStatementRow>> localReturnType = new ParameterizedTypeReference<List<AccountStatementRow>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/statement", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Returns current tip of db
     * 
     * <p><b>200</b> - OK
     * @return Long
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public Long getTip() throws RestClientException {
        return getTipWithHttpInfo().getBody();
    }

    /**
     * Returns current tip of db
     * 
     * <p><b>200</b> - OK
     * @return ResponseEntity&lt;Long&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Long> getTipWithHttpInfo() throws RestClientException {
        Object localVarPostBody = null;
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Long> localReturnType = new ParameterizedTypeReference<Long>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/tip", HttpMethod.GET, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * getTokenDetails
     * 
     * <p><b>200</b> - OK
     * @param policyId  (required)
     * @param assetName  (required)
     * @return TokenDetails
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TokenDetails getTokenDetails(String policyId, String assetName) throws RestClientException {
        return getTokenDetailsWithHttpInfo(policyId, assetName).getBody();
    }

    /**
     * getTokenDetails
     * 
     * <p><b>200</b> - OK
     * @param policyId  (required)
     * @param assetName  (required)
     * @return ResponseEntity&lt;TokenDetails&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TokenDetails> getTokenDetailsWithHttpInfo(String policyId, String assetName) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'policyId' is set
        if (policyId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'policyId' when calling getTokenDetails");
        }
        
        // verify the required parameter 'assetName' is set
        if (assetName == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'assetName' when calling getTokenDetails");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("policyId", policyId);
        uriVariables.put("assetName", assetName);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TokenDetails> localReturnType = new ParameterizedTypeReference<TokenDetails>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/token/{policyId}/{assetName}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * getTokenList
     * 
     * <p><b>200</b> - OK
     * @param afterMintid  (optional)
     * @param beforeMintid  (optional)
     * @param filter  (optional)
     * @return List&lt;TokenListItem&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TokenListItem> getTokenList(Long afterMintid, Long beforeMintid, String filter) throws RestClientException {
        return getTokenListWithHttpInfo(afterMintid, beforeMintid, filter).getBody();
    }

    /**
     * getTokenList
     * 
     * <p><b>200</b> - OK
     * @param afterMintid  (optional)
     * @param beforeMintid  (optional)
     * @param filter  (optional)
     * @return ResponseEntity&lt;List&lt;TokenListItem&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<TokenListItem>> getTokenListWithHttpInfo(Long afterMintid, Long beforeMintid, String filter) throws RestClientException {
        Object localVarPostBody = null;
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "afterMintid", afterMintid));
        localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "beforeMintid", beforeMintid));
        localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "filter", filter));
        

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<TokenListItem>> localReturnType = new ParameterizedTypeReference<List<TokenListItem>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/token", HttpMethod.GET, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get json metadata of tx
     * 
     * <p><b>200</b> - OK
     * @param txId  (required)
     * @return String
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public String getTransactionMetadata(String txId) throws RestClientException {
        return getTransactionMetadataWithHttpInfo(txId).getBody();
    }

    /**
     * Get json metadata of tx
     * 
     * <p><b>200</b> - OK
     * @param txId  (required)
     * @return ResponseEntity&lt;String&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<String> getTransactionMetadataWithHttpInfo(String txId) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'txId' is set
        if (txId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'txId' when calling getTransactionMetadata");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("txId", txId);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<String> localReturnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/transaction/{txId}/metadata", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get ada outputs if tx
     * 
     * <p><b>200</b> - OK
     * @param txId  (required)
     * @return List&lt;TxOut&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TxOut> getTransactionOutputs(String txId) throws RestClientException {
        return getTransactionOutputsWithHttpInfo(txId).getBody();
    }

    /**
     * Get ada outputs if tx
     * 
     * <p><b>200</b> - OK
     * @param txId  (required)
     * @return ResponseEntity&lt;List&lt;TxOut&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<TxOut>> getTransactionOutputsWithHttpInfo(String txId) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'txId' is set
        if (txId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'txId' when calling getTransactionOutputs");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("txId", txId);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<TxOut>> localReturnType = new ParameterizedTypeReference<List<TxOut>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/transaction/{txId}/outputs", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Find utxos of given address or stakeAddress including multi assets
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return List&lt;Utxo&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<Utxo> getUtxos(String address) throws RestClientException {
        return getUtxosWithHttpInfo(address).getBody();
    }

    /**
     * Find utxos of given address or stakeAddress including multi assets
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return ResponseEntity&lt;List&lt;Utxo&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<Utxo>> getUtxosWithHttpInfo(String address) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'address' is set
        if (address == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'address' when calling getUtxos");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("address", address);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<Utxo>> localReturnType = new ParameterizedTypeReference<List<Utxo>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/utxos", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Checks is a txid has been included in the chain
     * 
     * <p><b>200</b> - OK
     * @param txId  (required)
     * @return Boolean
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public Boolean isTransactionConfirmed(String txId) throws RestClientException {
        return isTransactionConfirmedWithHttpInfo(txId).getBody();
    }

    /**
     * Checks is a txid has been included in the chain
     * 
     * <p><b>200</b> - OK
     * @param txId  (required)
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Boolean> isTransactionConfirmedWithHttpInfo(String txId) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'txId' is set
        if (txId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'txId' when calling isTransactionConfirmed");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("txId", txId);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Boolean> localReturnType = new ParameterizedTypeReference<Boolean>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/transaction/{txId}/confirmed", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
