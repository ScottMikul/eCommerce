package com.acme.ecommerce.controller;

import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.service.ProductService;
import com.acme.ecommerce.service.PurchaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
@Scope("request")
public class CartController{
	final Logger logger = LoggerFactory.getLogger(CartController.class);
	
	@Autowired
	PurchaseService purchaseService;
	
	@Autowired
	private ProductService productService;

	@Autowired
	private ShoppingCart sCart;
	
	@Autowired
	private HttpSession session;
	
    @RequestMapping("")
    public String viewCart(Model model) {
    	logger.debug("Getting Product List");
    	logger.debug("Session ID = " + session.getId());
    	
    	Purchase purchase = sCart.getPurchase();
    	BigDecimal subTotal = new BigDecimal(0);
    	
    	model.addAttribute("purchase", purchase);
    	if (purchase != null) {
    		for (ProductPurchase pp : purchase.getProductPurchases()) {
    			logger.debug("cart has " + pp.getQuantity() + " of " + pp.getProduct().getName());
    			subTotal = subTotal.add(pp.getProduct().getPrice().multiply(new BigDecimal(pp.getQuantity())));
    		}
    		
    		model.addAttribute("subTotal", subTotal);
    	} else {
    		logger.error("No purchases Found for session ID=" + session.getId());
    		return "redirect:/error";
    	}
		if(sCart!=null)
			model.addAttribute("subtotal", sCart.getCartTotal());
        return "cart";
    }
    
    @RequestMapping(path="/add", method = RequestMethod.POST)
    public RedirectView addToCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="quantity") int quantity,
								  RedirectAttributes redirectAttributes) {
    	boolean productAlreadyInCart = false;
    	RedirectView redirect = new RedirectView("/product/");
		redirect.setExposeModelAttributes(false);
    	
    	Product addProduct = productService.findById(productId);

		sCart.getCartTotal();
		if (addProduct != null) {
	    	logger.debug("Adding Product: " + addProduct.getId());
	    	
    		Purchase purchase = sCart.getPurchase();
    		if (purchase == null) {
    			purchase = new Purchase();
    			sCart.setPurchase(purchase);
    		} else {
    			for (ProductPurchase pp : purchase.getProductPurchases()) {
    				if (pp.getProduct() != null) {
    					if (pp.getProduct().getId().equals(productId)) {
    						if (pp.getQuantity()+quantity> pp.getProduct().getQuantity()){
								logger.error("Purchase Quantity higher than product supply");
								redirectAttributes.addFlashAttribute("flashmessage","Purchase Quantity can not go higher than product supply!");
								return redirect;
							}
    						pp.setQuantity(pp.getQuantity() + quantity);
    						productAlreadyInCart = true;
    						break;
    					}
    				}
    			}
    		}
    		if (!productAlreadyInCart) {
    			if(addProduct.getQuantity()<quantity){
					redirectAttributes.addFlashAttribute("flashmessage","Purchase Quantity can not go higher than product supply!");
					return redirect;
				}
    			ProductPurchase newProductPurchase = new ProductPurchase();
    			newProductPurchase.setProduct(addProduct);
    			newProductPurchase.setQuantity(quantity);
    			newProductPurchase.setPurchase(purchase);
        		purchase.getProductPurchases().add(newProductPurchase);
    		}
    		logger.debug("Added " + quantity + " of " + addProduct.getName() + " to cart");
			redirectAttributes.addFlashAttribute("flashmessage","Added " + quantity +" "+ addProduct.getName() + " to cart");
    		sCart.setPurchase(purchaseService.save(purchase));
		} else {
			logger.error("Attempt to add unknown product: " + productId);
			redirect.setUrl("/error");
		}


    	return redirect;
    }
 
    @RequestMapping(path="/update", method = RequestMethod.POST)
    public RedirectView updateCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="newQuantity") int newQuantity,
								   RedirectAttributes redirectAttributes) {
    	logger.debug("Updating Product: " + productId + " with Quantity: " + newQuantity);
		RedirectView redirect = new RedirectView("/cart");
		redirect.setExposeModelAttributes(false);
    	
    	Product updateProduct = productService.findById(productId);
    	if (updateProduct != null) {
    		Purchase purchase = sCart.getPurchase();
    		if (purchase == null) {
    			logger.error("Unable to find shopping cart for update");
    			redirect.setUrl("/error");
    		} else {
    			for (ProductPurchase pp : purchase.getProductPurchases()) {
    				if (pp.getProduct() != null) {
    					if (pp.getProduct().getId().equals(productId)) {
    						if (newQuantity > 0) {
    							if(newQuantity> pp.getProduct().getQuantity()){
    								logger.error("Purchase quantity higher than product supply");
									redirectAttributes.addFlashAttribute("flashmessage","Not enough supply to increase purchase quantity to "+newQuantity+ "!");
									return redirect;
								}
    							pp.setQuantity(newQuantity);

								logger.debug("Updated " + updateProduct.getName() + " to " + newQuantity);
								redirectAttributes.addFlashAttribute("flashmessage","Updated " + updateProduct.getName() + " to " + newQuantity);
    						} else {
    							purchase.getProductPurchases().remove(pp);
    							logger.debug("Removed " + updateProduct.getName() + " because quantity was set to " + newQuantity);
								redirectAttributes.addFlashAttribute("flashmessage","Removed " + updateProduct.getName() + " because quantity was set to " + newQuantity);
    						}
    						break;
    					}
    				}
    			}
    		}
    		sCart.setPurchase(purchaseService.save(purchase));
    	} else {
    		logger.error("Attempt to update on non-existent product");
    		redirect.setUrl("/error");
    	}
    	
    	return redirect;
    }
    
    @RequestMapping(path="/remove", method = RequestMethod.POST)
    public RedirectView removeFromCart(@ModelAttribute(value="productId") long productId, RedirectAttributes redirectAttributes) {
    	logger.debug("Removing Product: " + productId);
		RedirectView redirect = new RedirectView("/cart");
		redirect.setExposeModelAttributes(false);
    	
    	Product updateProduct = productService.findById(productId);
    	if (updateProduct != null) {
    		Purchase purchase = sCart.getPurchase();
    		if (purchase != null) {
    			for (ProductPurchase pp : purchase.getProductPurchases()) {
    				if (pp.getProduct() != null) {
    					if (pp.getProduct().getId().equals(productId)) {
    						purchase.getProductPurchases().remove(pp);
							redirectAttributes.addFlashAttribute("flashmessage","Removed " + updateProduct.getName());
   							logger.debug("Removed " + updateProduct.getName());
    						break;
    					}
    				}
    			}
    			purchase = purchaseService.save(purchase);
    			sCart.setPurchase(purchase);
    			if (purchase.getProductPurchases().isEmpty()) {
        	    	//if last item in cart redirect to product else return cart
        			redirect.setUrl("/product/");
        		}
    		} else {
    			logger.error("Unable to find shopping cart for update");
    			redirect.setUrl("/error");
    		}
    	} else {
    		logger.error("Attempt to update on non-existent product");
    		redirect.setUrl("/error");
    	}

    	return redirect;
    }
    
    @RequestMapping(path="/empty", method = RequestMethod.POST)
    public RedirectView emptyCart(RedirectAttributes redirectAttributes) {
    	RedirectView redirect = new RedirectView("/product/");
		redirect.setExposeModelAttributes(false);
    	
    	logger.debug("Emptying Cart");
    	Purchase purchase = sCart.getPurchase();
		if (purchase != null) {
			purchase.getProductPurchases().clear();
			redirectAttributes.addFlashAttribute("flashmessage","Emptied cart");
			sCart.setPurchase(purchaseService.save(purchase));
		} else {
			logger.error("Unable to find shopping cart for update");
			redirect.setUrl("/error");
		}
		
    	return redirect;
    }
}
