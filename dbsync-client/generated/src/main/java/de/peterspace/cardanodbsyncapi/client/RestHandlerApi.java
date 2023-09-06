package de.peterspace.cardanodbsyncapi.client;

import de.peterspace.cardanodbsyncapi.client.ApiClient;

import de.peterspace.cardanodbsyncapi.client.model.PoolInfo;
import de.peterspace.cardanodbsyncapi.client.model.ReturnAddress;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-09-06T19:26:55.813438100+02:00[Europe/Berlin]")
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
            "*/*"
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
     * @param address  (required)
     * @return ReturnAddress
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ReturnAddress getReturnAddress(String address) throws RestClientException {
        return getReturnAddressWithHttpInfo(address).getBody();
    }

    /**
     * Find the first known address with the same stake address, which should not be mangled
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return ResponseEntity&lt;ReturnAddress&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<ReturnAddress> getReturnAddressWithHttpInfo(String address) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'address' is set
        if (address == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'address' when calling getReturnAddress");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("address", address);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<ReturnAddress> localReturnType = new ParameterizedTypeReference<ReturnAddress>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/returnAddress", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get infos where address is staked to
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return StakeInfo
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StakeInfo getStakeInfo(String address) throws RestClientException {
        return getStakeInfoWithHttpInfo(address).getBody();
    }

    /**
     * Get infos where address is staked to
     * 
     * <p><b>200</b> - OK
     * @param address  (required)
     * @return ResponseEntity&lt;StakeInfo&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StakeInfo> getStakeInfoWithHttpInfo(String address) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'address' is set
        if (address == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'address' when calling getStakeInfo");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("address", address);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<StakeInfo> localReturnType = new ParameterizedTypeReference<StakeInfo>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/stakeInfo", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
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
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<TokenListItem>> localReturnType = new ParameterizedTypeReference<List<TokenListItem>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/token", HttpMethod.GET, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
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
    public TokenDetails getTokenList1(String policyId, String assetName) throws RestClientException {
        return getTokenList1WithHttpInfo(policyId, assetName).getBody();
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
    public ResponseEntity<TokenDetails> getTokenList1WithHttpInfo(String policyId, String assetName) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'policyId' is set
        if (policyId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'policyId' when calling getTokenList1");
        }
        
        // verify the required parameter 'assetName' is set
        if (assetName == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'assetName' when calling getTokenList1");
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
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TokenDetails> localReturnType = new ParameterizedTypeReference<TokenDetails>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/token/{policyId}/{assetName}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Find utxos of given address including multi assets
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
     * Find utxos of given address including multi assets
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
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<List<Utxo>> localReturnType = new ParameterizedTypeReference<List<Utxo>>() {};
        return apiClient.invokeAPI("/cardanoDbSyncApi/{address}/utxos", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
