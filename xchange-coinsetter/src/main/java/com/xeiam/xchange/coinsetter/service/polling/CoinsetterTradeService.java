package com.xeiam.xchange.coinsetter.service.polling;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.NotAvailableFromExchangeException;
import com.xeiam.xchange.coinsetter.CoinsetterAdapters;
import com.xeiam.xchange.coinsetter.dto.clientsession.response.CoinsetterClientSession;
import com.xeiam.xchange.coinsetter.dto.order.request.CoinsetterOrderRequest;
import com.xeiam.xchange.coinsetter.dto.order.response.CoinsetterOrderList;
import com.xeiam.xchange.coinsetter.dto.order.response.CoinsetterOrderResponse;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.UserTrades;
import com.xeiam.xchange.service.polling.PollingTradeService;

/**
 * Trade service.
 */
public class CoinsetterTradeService extends CoinsetterBasePollingService implements PollingTradeService {

  private final CoinsetterOrderServiceRaw orderServiceRaw;

  /**
   * @param exchangeSpecification
   */
  public CoinsetterTradeService(ExchangeSpecification exchangeSpecification) {

    super(exchangeSpecification);
    orderServiceRaw = new CoinsetterOrderServiceRaw(exchangeSpecification);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OpenOrders getOpenOrders() throws IOException {

    CoinsetterClientSession session = getSession();
    CoinsetterOrderList orderList = orderServiceRaw.list(session.getUuid(), getAccountUuid(), "OPEN");
    return CoinsetterAdapters.adaptOpenOrders(orderList);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {

    return add(marketOrder.getCurrencyPair(), marketOrder.getType(), marketOrder.getTradableAmount(), null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {

    return add(limitOrder.getCurrencyPair(), limitOrder.getType(), limitOrder.getTradableAmount(), limitOrder.getLimitPrice());
  }

  private String add(CurrencyPair currencyPair, OrderType orderType, BigDecimal quantity, BigDecimal price) throws IOException {

    CoinsetterClientSession session = getSession();
    CoinsetterOrderRequest request =
        new CoinsetterOrderRequest(session.getCustomerUuid(), getAccountUuid(), CoinsetterAdapters.adaptSymbol(currencyPair), CoinsetterAdapters.adaptSide(orderType), price == null ? "MARKET"
            : "LIMIT", quantity, 2, price);
    return orderServiceRaw.add(getSession().getUuid(), request).getUuid().toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean cancelOrder(String orderId) throws IOException {

    CoinsetterOrderResponse response = orderServiceRaw.cancel(getSession().getUuid(), UUID.fromString(orderId));
    return "SUCCESS".equals(response.getRequestStatus());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UserTrades getTradeHistory(Object... arguments) throws IOException {

    throw new NotAvailableFromExchangeException();
  }

}
