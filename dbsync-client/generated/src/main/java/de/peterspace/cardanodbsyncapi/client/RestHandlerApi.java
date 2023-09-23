package de.peterspace.cardanodbsyncapi.client;

import de.peterspace.cardanodbsyncapi.client.ApiClient;

import de.peterspace.cardanodbsyncapi.client.model.AccountStatementRow;
import de.peterspace.cardanodbsyncapi.client.model.EpochStake;
import de.peterspace.cardanodbsyncapi.client.model.PoolInfo;
import de.peterspace.cardanodbsyncapi.client.model.ReturnAddress;
import de.peterspace.cardanodbsyncapi.client.model.StakeAddress;
import de.peterspace.cardanodbsyncapi.client.model.StakeInfo;
import de.peterspace.cardanodbsyncapi.client.model.TokenDetails;
import de.peterspace.cardanodbsyncapi.client.model.TokenListItem;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-09-23T22:00:56.355312+02:00[Europe/Berlin]")
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
}
