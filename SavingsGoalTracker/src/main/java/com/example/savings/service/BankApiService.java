package com.example.savings.service;

import com.example.savings.model.BankAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BankApiService {
  private static final Logger logger = LoggerFactory.getLogger(BankApiService.class);

  @Value("${banking.api.clientId}")
  private String clientId;

  @Value("${banking.api.secret}")
  private String secret;

  @Value("${banking.api.environment}")
  private String environment;

  @Value("${banking.api.baseurl}")
  private String baseUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  public Double getAccountBalance(BankAccount account) {
    try {
      String url = baseUrl + "/accounts/" + account.getAccountNumber() + "/balance";
      HttpHeaders headers = createAuthHeaders(account.getAccessToken());
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> balanceData = (Map<String, Object>) responseBody.get("balance");

        return Double.parseDouble(balanceData.get("available").toString());
    } catch (Exception e) {
      logger.error("Error fetching account balance", e);
      throw new RuntimeException("Failed to fetch account balance: " + e.getMessage());
    }
  }

  public boolean transferFunds(BankAccount sourceAccount, BankAccount destinationAccount, Double amount) {
    try {
      String url = baseUrl + "/transfers";

      HttpHeaders headers = createAuthHeaders(sourceAccount.getAccessToken());

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("Source_amount_id", sourceAccount.getAccountNumber());
      requestBody.put("destination_account_id", destinationAccount.getAccountNumber());
      requestBody.put("amount", amount);
      requestBody.put("currency", "USD");

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        String status = (String) responseBody.get("status");

        return "completed".equals(status) || "pending".equals(status);
    } catch (Exception e) {
      logger.error("Error transferring funds", e);
      throw new RuntimeException("Failed to transfer funds: " + e.getMessage());
    }
  }

  public List<Map<String, Object>> getTransactionsSince(BankAccount account, LocalDateTime startDate) {
    try {
      String formattedDate = startDate.format(DateTimeFormatter.ISO_DATE_TIME);
      String url = baseUrl + "/accounts/" + account.getAccountNumber() + "/transactions?start_date=" + formattedDate;

      HttpHeaders headers = createAuthHeaders(account.getAccessToken());
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.GET, entity, Map.class);

      Map<String, Object> responseBody = response.getBody();
      return (List<Map<String, Object>>) responseBody.get("transactions");
    } catch (Exception e) {
      logger.error("Error fetching transactions", e);
      throw new RuntimeException("Failed to fetch transactions: " + e.getMessage());
    }
  }

  public String linkBankAccount(String publicToken, String accountId) {
    try {
      String url = baseUrl + "/item/public_token/exchange";

      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");
      headers.set("Client-ID", clientId);
      headers.set("secret", secret);

      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("public_token", publicToken);
      requestBody.put("account_id", accountId);

      HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<Map> response = restTemplate.exchange(
        url, HttpMethod.POST, entity, Map.class);
      Map<String, Object> responseBody = response.getBody();
      return (String) responseBody.get("access_token");
    } catch (Exception e) {
      logger.error("Error linking bank account", e);
      throw new RuntimeException("Failed to link bank account: " + e.getMessage());
    }
  }

  private HttpHeaders createAuthHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    headers.set("Authorization", "Bearer " + accessToken);
    headers.set("Client-ID", clientId);
    headers.set("Secret", secret);
    return headers;
  }
}
