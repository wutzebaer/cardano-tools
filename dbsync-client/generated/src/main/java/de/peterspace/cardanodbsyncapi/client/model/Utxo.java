/*
 * Cardano DB-Sync API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package de.peterspace.cardanodbsyncapi.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Utxo
 */
@JsonPropertyOrder({
  Utxo.JSON_PROPERTY_TX_HASH,
  Utxo.JSON_PROPERTY_TX_INDEX,
  Utxo.JSON_PROPERTY_MA_POLICY_ID,
  Utxo.JSON_PROPERTY_MA_NAME,
  Utxo.JSON_PROPERTY_VALUE,
  Utxo.JSON_PROPERTY_SOURCE_ADDRESS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-09-23T22:00:56.355312+02:00[Europe/Berlin]")
public class Utxo {
  public static final String JSON_PROPERTY_TX_HASH = "txHash";
  private String txHash;

  public static final String JSON_PROPERTY_TX_INDEX = "txIndex";
  private Integer txIndex;

  public static final String JSON_PROPERTY_MA_POLICY_ID = "maPolicyId";
  private String maPolicyId;

  public static final String JSON_PROPERTY_MA_NAME = "maName";
  private String maName;

  public static final String JSON_PROPERTY_VALUE = "value";
  private Long value;

  public static final String JSON_PROPERTY_SOURCE_ADDRESS = "sourceAddress";
  private String sourceAddress;

  public Utxo() {
  }

  public Utxo txHash(String txHash) {
    
    this.txHash = txHash;
    return this;
  }

   /**
   * Get txHash
   * @return txHash
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TX_HASH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getTxHash() {
    return txHash;
  }


  @JsonProperty(JSON_PROPERTY_TX_HASH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTxHash(String txHash) {
    this.txHash = txHash;
  }


  public Utxo txIndex(Integer txIndex) {
    
    this.txIndex = txIndex;
    return this;
  }

   /**
   * Get txIndex
   * @return txIndex
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TX_INDEX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Integer getTxIndex() {
    return txIndex;
  }


  @JsonProperty(JSON_PROPERTY_TX_INDEX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTxIndex(Integer txIndex) {
    this.txIndex = txIndex;
  }


  public Utxo maPolicyId(String maPolicyId) {
    
    this.maPolicyId = maPolicyId;
    return this;
  }

   /**
   * Get maPolicyId
   * @return maPolicyId
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MA_POLICY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getMaPolicyId() {
    return maPolicyId;
  }


  @JsonProperty(JSON_PROPERTY_MA_POLICY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMaPolicyId(String maPolicyId) {
    this.maPolicyId = maPolicyId;
  }


  public Utxo maName(String maName) {
    
    this.maName = maName;
    return this;
  }

   /**
   * Get maName
   * @return maName
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MA_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getMaName() {
    return maName;
  }


  @JsonProperty(JSON_PROPERTY_MA_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMaName(String maName) {
    this.maName = maName;
  }


  public Utxo value(Long value) {
    
    this.value = value;
    return this;
  }

   /**
   * Get value
   * @return value
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_VALUE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getValue() {
    return value;
  }


  @JsonProperty(JSON_PROPERTY_VALUE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setValue(Long value) {
    this.value = value;
  }


  public Utxo sourceAddress(String sourceAddress) {
    
    this.sourceAddress = sourceAddress;
    return this;
  }

   /**
   * Get sourceAddress
   * @return sourceAddress
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SOURCE_ADDRESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getSourceAddress() {
    return sourceAddress;
  }


  @JsonProperty(JSON_PROPERTY_SOURCE_ADDRESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourceAddress(String sourceAddress) {
    this.sourceAddress = sourceAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Utxo utxo = (Utxo) o;
    return Objects.equals(this.txHash, utxo.txHash) &&
        Objects.equals(this.txIndex, utxo.txIndex) &&
        Objects.equals(this.maPolicyId, utxo.maPolicyId) &&
        Objects.equals(this.maName, utxo.maName) &&
        Objects.equals(this.value, utxo.value) &&
        Objects.equals(this.sourceAddress, utxo.sourceAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(txHash, txIndex, maPolicyId, maName, value, sourceAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Utxo {\n");
    sb.append("    txHash: ").append(toIndentedString(txHash)).append("\n");
    sb.append("    txIndex: ").append(toIndentedString(txIndex)).append("\n");
    sb.append("    maPolicyId: ").append(toIndentedString(maPolicyId)).append("\n");
    sb.append("    maName: ").append(toIndentedString(maName)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    sourceAddress: ").append(toIndentedString(sourceAddress)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

