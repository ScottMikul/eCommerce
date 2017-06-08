package com.acme.ecommerce.domain;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component
@Scope("session")
public class ShoppingCart{

	private Purchase purchase = null;
	private CouponCode couponCode = null;
	private BigDecimal cartTotal = null;

	public Purchase getPurchase() {
		return purchase;
	}

	public void setPurchase(Purchase purchase) {
		this.purchase = purchase;
	}

	public CouponCode getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(CouponCode couponCode) {
		this.couponCode = couponCode;
	}

	public String getCartTotal(){
		cartTotal = new BigDecimal(0);
		if(getPurchase()!=null) {
			for (ProductPurchase purchase : getPurchase().getProductPurchases()) {
				BigDecimal price = purchase.getProduct().getPrice();
				BigDecimal quantity = new BigDecimal(purchase.getQuantity());
				cartTotal=cartTotal.add(price.multiply(quantity));
			}
		}
		DecimalFormat formatter = new DecimalFormat("#,###.##");
		String res ="$"+ formatter.format(cartTotal);
		return res;
	}



}
