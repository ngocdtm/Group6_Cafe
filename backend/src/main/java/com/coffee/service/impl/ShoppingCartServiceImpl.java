package com.coffee.service.impl;

import com.coffee.constants.CafeConstants;
import com.coffee.entity.CartItems;
import com.coffee.entity.Product;
import com.coffee.entity.ShoppingCart;
import com.coffee.entity.User;
import com.coffee.repository.CartItemsRepository;
import com.coffee.repository.ProductRepository;
import com.coffee.repository.ShoppingCartRepository;
import com.coffee.repository.UserRepository;
import com.coffee.security.JwtRequestFilter;
import com.coffee.security.ResourceNotFoundException;
import com.coffee.service.InventoryService;
import com.coffee.service.ShoppingCartService;
import com.coffee.utils.CafeUtils;
import com.coffee.wrapper.CartItemWrapper;
import com.coffee.wrapper.InventoryWrapper;
import com.coffee.wrapper.ShoppingCartWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    @Autowired
    private ShoppingCartRepository cartRepository;

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public ResponseEntity<String> addToCart(Map<String, String> requestMap) {
        try {
            if (validateCartMap(requestMap)) {
                User user = userRepository.findByEmail(jwtRequestFilter.getCurrentUser());
                Product product = productRepository.findById(Integer.parseInt(requestMap.get("productId")))
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // Kiểm tra số lượng hàng tồn kho
                ResponseEntity<InventoryWrapper> inventoryStatus = inventoryService.getInventoryStatus(product.getId());
                if (inventoryStatus.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return CafeUtils.getResponseEntity("Product not found in inventory", HttpStatus.NOT_FOUND);
                }
                InventoryWrapper inventory = inventoryStatus.getBody();
                int availableQuantity = inventory.getQuantity();

                // Kiểm tra số lượng sản phẩm hiện tại trong giỏ
                ShoppingCart cart = cartRepository.findByUser(user).orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUser(user);
                    newCart.setTotalAmount(0);
                    return cartRepository.save(newCart);
                });

                Optional<CartItems> existingItem = cart.getCartItems().stream()
                        .filter(item -> item.getProduct().getId().equals(product.getId()))
                        .findFirst();

                int currentQuantityInCart = existingItem.map(CartItems::getQuantity).orElse(0);
                int requestedQuantity = Integer.parseInt(requestMap.get("quantity"));

                // Nếu tổng số lượng sản phẩm trong giỏ hàng >= tồn kho, không cho phép thêm vào
                if (currentQuantityInCart + requestedQuantity > availableQuantity) {
                    return CafeUtils.getResponseEntity("Không thể thêm vào giỏ hàng. Sản phẩm đã đạt đến giới hạn tồn kho.", HttpStatus.BAD_REQUEST);
                }

                // Thực hiện thêm vào giỏ nếu chưa đạt giới hạn
                if (existingItem.isPresent()) {
                    existingItem.get().setQuantity(currentQuantityInCart + requestedQuantity);
                } else {
                    CartItems cartItem = new CartItems();
                    cartItem.setProduct(product);
                    cartItem.setShoppingCart(cart);
                    cartItem.setQuantity(requestedQuantity);
                    cartItem.setPrice(product.getPrice());
                    cart.getCartItems().add(cartItem);
                }

                updateCartTotal(cart);
                cartRepository.save(cart);
                return CafeUtils.getResponseEntity("Added to cart successfully", HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> updateCartItem(Map<String, String> requestMap) {
        try {
            // Validate input data first
            if (!requestMap.containsKey("cartItemId") || !requestMap.containsKey("quantity")) {
                return CafeUtils.getResponseEntity("Missing required fields", HttpStatus.BAD_REQUEST);
            }

            Integer cartItemId = Integer.parseInt(requestMap.get("cartItemId"));
            Integer quantity = Integer.parseInt(requestMap.get("quantity"));

            if (quantity < 0) {
                return CafeUtils.getResponseEntity("Invalid quantity", HttpStatus.BAD_REQUEST);
            }

            CartItems cartItem = cartItemsRepository.findById(cartItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

            // Kiểm tra quyền truy cập
            if (!cartItem.getShoppingCart().getUser().getEmail().equals(jwtRequestFilter.getCurrentUser())) {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

            Product product = cartItem.getProduct();

            // Kiểm tra trạng thái sản phẩm
            if ("OUT_OF_STOCK".equals(product.getStatus()) || !"true".equals(product.getStatus())) {
                // Tự động xóa item khỏi giỏ hàng nếu sản phẩm không còn available
                return removeFromCart(cartItemId);
            }

            // Kiểm tra inventory
            ResponseEntity<InventoryWrapper> inventoryStatus = inventoryService.getInventoryStatus(product.getId());
            if (inventoryStatus.getStatusCode() == HttpStatus.NOT_FOUND) {
                return CafeUtils.getResponseEntity("Product not found in inventory", HttpStatus.NOT_FOUND);
            }

            InventoryWrapper inventory = inventoryStatus.getBody();
            if (inventory == null) {
                return CafeUtils.getResponseEntity("Invalid inventory data", HttpStatus.BAD_REQUEST);
            }

            if (quantity > inventory.getQuantity()) {
                return CafeUtils.getResponseEntity(
                        String.format("Insufficient stock. Available: %d, Requested: %d",
                                inventory.getQuantity(), quantity),
                        HttpStatus.BAD_REQUEST);
            }

            if (quantity == 0) {
                return removeFromCart(cartItemId);
            }

            // Cập nhật số lượng và giá mới nhất
            cartItem.setQuantity(quantity);
            cartItem.setPrice(product.getPrice());
            updateCartTotal(cartItem.getShoppingCart());
            cartItemsRepository.save(cartItem);

            return CafeUtils.getResponseEntity("Cart updated successfully", HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return CafeUtils.getResponseEntity(ex.getMessage(), HttpStatus.NOT_FOUND);
        } catch (NumberFormatException e) {
            return CafeUtils.getResponseEntity("Invalid number format", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            logger.error("Error updating cart: ", ex);
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> removeFromCart(Integer cartItemId) {
        try {
            CartItems cartItem = cartItemsRepository.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            if (!cartItem.getShoppingCart().getUser().getEmail().equals(jwtRequestFilter.getCurrentUser())) {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

            ShoppingCart cart = cartItem.getShoppingCart();
            cart.getCartItems().remove(cartItem);
            updateCartTotal(cart);
            cartRepository.save(cart);

            return CafeUtils.getResponseEntity("Item removed from cart", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<ShoppingCartWrapper> getCart() {
        try {
            User user = userRepository.findByEmail(jwtRequestFilter.getCurrentUser());
            ShoppingCart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        ShoppingCart newCart = new ShoppingCart();
                        newCart.setUser(user);
                        newCart.setTotalAmount(0);
                        return cartRepository.save(newCart);
                    });

            // Kiểm tra và cập nhật trạng thái các sản phẩm trong giỏ hàng
            boolean hasOutOfStockItems = false;
            for (CartItems item : cart.getCartItems()) {
                Product product = item.getProduct();
                ResponseEntity<InventoryWrapper> inventoryStatus = inventoryService.getInventoryStatus(product.getId());

                // Kiểm tra trạng thái sản phẩm và inventory
                boolean isOutOfStock = "OUT_OF_STOCK".equals(product.getStatus())
                        || !"true".equals(product.getStatus())
                        || inventoryStatus.getStatusCode() == HttpStatus.NOT_FOUND
                        || (inventoryStatus.getBody() != null && inventoryStatus.getBody().getQuantity() < item.getQuantity());

                if (isOutOfStock) {
                    hasOutOfStockItems = true;
                }
            }
            ShoppingCartWrapper cartWrapper = convertToDTO(cart);
            cartWrapper.setHasOutOfStockItems(hasOutOfStockItems);
            return new ResponseEntity<>(cartWrapper, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("Error fetching cart: ", ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ShoppingCartWrapper convertToDTO(ShoppingCart cart) {
        ShoppingCartWrapper dto = new ShoppingCartWrapper();
        dto.setId(cart.getId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setUserId(cart.getUser().getId());

        if (cart.getCartItems() != null) {
            dto.setCartItems(cart.getCartItems().stream()
                    .map(this::convertToCartItemDTO)
                    .collect(Collectors.toList()));
        } else {
            logger.warn("Cart items list is null for cart ID: {}", cart.getId());
            dto.setCartItems(Collections.emptyList());
        }
        return dto;
    }

    private CartItemWrapper convertToCartItemDTO(CartItems item) {
        CartItemWrapper dto = new CartItemWrapper();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
        } else {
            logger.warn("Product is null for cart item ID: {}", item.getId());
        }
        return dto;
    }

    @Override
    @Transactional
    public ResponseEntity<String> clearCart() {
        try {
            User user = userRepository.findByEmail(jwtRequestFilter.getCurrentUser());
            Optional<ShoppingCart> cart = cartRepository.findByUser(user);


            if (cart.isPresent()) {
                cart.get().getCartItems().clear();
                cart.get().setTotalAmount(0);
                cartRepository.save(cart.get());
            }
            return CafeUtils.getResponseEntity("Cart cleared successfully", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public ResponseEntity<Integer> getCartItemCount() {
        try {
            User user = userRepository.findByEmail(jwtRequestFilter.getCurrentUser());
            Optional<ShoppingCart> cartOptional = cartRepository.findByUser(user);

            if (cartOptional.isPresent()) {
                ShoppingCart cart = cartOptional.get();
                int uniqueItemCount = (int) cart.getCartItems().stream()
                        .map(CartItems::getProduct)
                        .distinct()
                        .count();
                return new ResponseEntity<>(uniqueItemCount, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(0, HttpStatus.OK);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateCartMap(Map<String, String> requestMap) {
        return requestMap.containsKey("productId") && requestMap.containsKey("quantity");
    }

    private void updateCartTotal(ShoppingCart cart) {
        int total = cart.getCartItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
        cart.setTotalAmount(total);
    }
}