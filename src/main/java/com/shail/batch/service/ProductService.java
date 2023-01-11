package com.shail.batch.service;

import com.shail.batch.entity.Product;
import com.shail.batch.entity.ProductFinal;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    public ProductFinal processProducts (Product product) {
        String ogPrice = "" + Double.parseDouble(product.getPrice()) * 1.10;
        String onForSale = "Y".equals(product.getOnForSale()) ? "N" : "N";
        ProductFinal prd = new ProductFinal(product.getId(), product.getName(), ogPrice, product.getDescription(), product.getWeight(), product.getBrand(), onForSale);
        return prd;
    }
}
