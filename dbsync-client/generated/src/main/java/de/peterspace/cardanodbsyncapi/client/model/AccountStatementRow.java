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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * AccountStatementRow
 */
@JsonPropertyOrder({
  AccountStatementRow.JSON_PROPERTY_TIMESTAMP,
  AccountStatementRow.JSON_PROPERTY_EPOCH,
  AccountStatementRow.JSON_PROPERTY_TX_HASH,
  AccountStatementRow.JSON_PROPERTY_WITHDRAWN,
  AccountStatementRow.JSON_PROPERTY_REWARDS,
  AccountStatementRow.JSON_PROPERTY_OUT,
  AccountStatementRow.JSON_PROPERTY_IN,
  AccountStatementRow.JSON_PROPERTY_CHANGE,
  AccountStatementRow.JSON_PROPERTY_SUM,
  AccountStatementRow.JSON_PROPERTY_OPERATIONS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-03-06T20:43:49.743375300+01:00[Europe/Berlin]")
public class AccountStatementRow {
  public static final String JSON_PROPERTY_TIMESTAMP = "timestamp";
  private OffsetDateTime timestamp;

  public static final String JSON_PROPERTY_EPOCH = "epoch";
  private Integer epoch;

  public static final String JSON_PROPERTY_TX_HASH = "txHash";
  private String txHash;

  public static final String JSON_PROPERTY_WITHDRAWN = "withdrawn";
  private Long withdrawn;

  public static final String JSON_PROPERTY_REWARDS = "rewards";
  private Long rewards;

  public static final String JSON_PROPERTY_OUT = "out";
  private Long out;

  public static final String JSON_PROPERTY_IN = "in";
  private Long in;

  public static final String JSON_PROPERTY_CHANGE = "change";
  private Long change;

  public static final String JSON_PROPERTY_SUM = "sum";
  private Long sum;

  public static final String JSON_PROPERTY_OPERATIONS = "operations";
  private List<String> operations = new ArrayList<>();

  public AccountStatementRow() {
  }

  public AccountStatementRow timestamp(OffsetDateTime timestamp) {
    
    this.timestamp = timestamp;
    return this;
  }

   /**
   * Get timestamp
   * @return timestamp
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TIMESTAMP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }


  @JsonProperty(JSON_PROPERTY_TIMESTAMP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }


  public AccountStatementRow epoch(Integer epoch) {
    
    this.epoch = epoch;
    return this;
  }

   /**
   * Get epoch
   * @return epoch
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_EPOCH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Integer getEpoch() {
    return epoch;
  }


  @JsonProperty(JSON_PROPERTY_EPOCH)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEpoch(Integer epoch) {
    this.epoch = epoch;
  }


  public AccountStatementRow txHash(String txHash) {
    
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


  public AccountStatementRow withdrawn(Long withdrawn) {
    
    this.withdrawn = withdrawn;
    return this;
  }

   /**
   * Get withdrawn
   * @return withdrawn
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_WITHDRAWN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getWithdrawn() {
    return withdrawn;
  }


  @JsonProperty(JSON_PROPERTY_WITHDRAWN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWithdrawn(Long withdrawn) {
    this.withdrawn = withdrawn;
  }


  public AccountStatementRow rewards(Long rewards) {
    
    this.rewards = rewards;
    return this;
  }

   /**
   * Get rewards
   * @return rewards
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REWARDS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getRewards() {
    return rewards;
  }


  @JsonProperty(JSON_PROPERTY_REWARDS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRewards(Long rewards) {
    this.rewards = rewards;
  }


  public AccountStatementRow out(Long out) {
    
    this.out = out;
    return this;
  }

   /**
   * Get out
   * @return out
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_OUT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getOut() {
    return out;
  }


  @JsonProperty(JSON_PROPERTY_OUT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOut(Long out) {
    this.out = out;
  }


  public AccountStatementRow in(Long in) {
    
    this.in = in;
    return this;
  }

   /**
   * Get in
   * @return in
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_IN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getIn() {
    return in;
  }


  @JsonProperty(JSON_PROPERTY_IN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIn(Long in) {
    this.in = in;
  }


  public AccountStatementRow change(Long change) {
    
    this.change = change;
    return this;
  }

   /**
   * Get change
   * @return change
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CHANGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getChange() {
    return change;
  }


  @JsonProperty(JSON_PROPERTY_CHANGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setChange(Long change) {
    this.change = change;
  }


  public AccountStatementRow sum(Long sum) {
    
    this.sum = sum;
    return this;
  }

   /**
   * Get sum
   * @return sum
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SUM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Long getSum() {
    return sum;
  }


  @JsonProperty(JSON_PROPERTY_SUM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSum(Long sum) {
    this.sum = sum;
  }


  public AccountStatementRow operations(List<String> operations) {
    
    this.operations = operations;
    return this;
  }

  public AccountStatementRow addOperationsItem(String operationsItem) {
    if (this.operations == null) {
      this.operations = new ArrayList<>();
    }
    this.operations.add(operationsItem);
    return this;
  }

   /**
   * Get operations
   * @return operations
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_OPERATIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<String> getOperations() {
    return operations;
  }


  @JsonProperty(JSON_PROPERTY_OPERATIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOperations(List<String> operations) {
    this.operations = operations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountStatementRow accountStatementRow = (AccountStatementRow) o;
    return Objects.equals(this.timestamp, accountStatementRow.timestamp) &&
        Objects.equals(this.epoch, accountStatementRow.epoch) &&
        Objects.equals(this.txHash, accountStatementRow.txHash) &&
        Objects.equals(this.withdrawn, accountStatementRow.withdrawn) &&
        Objects.equals(this.rewards, accountStatementRow.rewards) &&
        Objects.equals(this.out, accountStatementRow.out) &&
        Objects.equals(this.in, accountStatementRow.in) &&
        Objects.equals(this.change, accountStatementRow.change) &&
        Objects.equals(this.sum, accountStatementRow.sum) &&
        Objects.equals(this.operations, accountStatementRow.operations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, epoch, txHash, withdrawn, rewards, out, in, change, sum, operations);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountStatementRow {\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    epoch: ").append(toIndentedString(epoch)).append("\n");
    sb.append("    txHash: ").append(toIndentedString(txHash)).append("\n");
    sb.append("    withdrawn: ").append(toIndentedString(withdrawn)).append("\n");
    sb.append("    rewards: ").append(toIndentedString(rewards)).append("\n");
    sb.append("    out: ").append(toIndentedString(out)).append("\n");
    sb.append("    in: ").append(toIndentedString(in)).append("\n");
    sb.append("    change: ").append(toIndentedString(change)).append("\n");
    sb.append("    sum: ").append(toIndentedString(sum)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
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

