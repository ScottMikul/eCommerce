package com.acme.ecommerce.controller;

import com.acme.ecommerce.Application;
import com.acme.ecommerce.domain.CouponCode;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.service.ProductService;
import com.acme.ecommerce.service.PurchaseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by scott on 6/4/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class CheckoutControllerCouponTest {

    @InjectMocks
    private CheckoutController CheckoutController;

    private MockMvc mockMvc;

    @Mock
    private ShoppingCart sCart;

    static {
        System.setProperty("properties.home", "properties");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(CheckoutController).build();
    }

    @Test
    public void PurchaseWithoutCouponIsOk() throws Exception {
        mockMvc.perform(post("/checkout/coupon").param("code",""))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("shipping"));

    }
    @Test
    public void PurchaseWithCouponSmallerThanSizeIsNotOk() throws Exception {
        mockMvc.perform(post("/checkout/coupon").param("code","asdf"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("coupon"));
    }
    @Test
    public void PurchaseWithCouponWithCorrectSizeIsOk() throws Exception {
        mockMvc.perform(post("/checkout/coupon").param("code","12345"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("shipping"));


    }
    @Test
    public void PurchaseWithCouponBiggerThanSizeIsNotOk() throws Exception {
        mockMvc.perform(post("/checkout/coupon").param("code","123456789101112"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("coupon"));

    }

}
