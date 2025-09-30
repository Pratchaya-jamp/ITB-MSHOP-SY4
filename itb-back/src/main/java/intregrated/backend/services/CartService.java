package intregrated.backend.services;

import intregrated.backend.dtos.carts.CartItemResponseDto;
import intregrated.backend.dtos.carts.CartRequestDto;
import intregrated.backend.dtos.carts.CartResponseDto;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.entities.carts.Cart;
import intregrated.backend.entities.carts.CartItem;
import intregrated.backend.entities.saleitems.SaleItemBase;
import intregrated.backend.repositories.CartItemRepo;
import intregrated.backend.repositories.CartRepo;
import intregrated.backend.repositories.SaleItemBaseRepo;
import intregrated.backend.repositories.UsersAccountRepo;
import intregrated.backend.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class CartService {
    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    @Autowired
    private SaleItemBaseRepo saleItemRepo;

    @Autowired
    private UsersAccountRepo usersAccountRepo;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Transactional
    public CartResponseDto addToCart(CartRequestDto request, Integer userId) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not contain buyer_id");
        }

        UsersAccount buyer = usersAccountRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));

        // หา/สร้าง cart ของ buyer
        Cart cart = cartRepo.findByBuyer(buyer)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setBuyer(buyer);
                    c.setCreatedOn(Instant.now());
                    c.setUpdatedOn(Instant.now());
                    return cartRepo.save(c);
                });

        for (CartRequestDto.CartItemDto dto : request.getItems()) {
            SaleItemBase saleItem = saleItemRepo.findById(dto.getSaleItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

            // ห้ามซื้อสินค้าของตัวเอง
            if (saleItem.getSeller() != null && buyer.getSeller() != null &&
                    saleItem.getSeller().getId().equals(buyer.getSeller().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add your own product");
            }

            // Stock ไม่พอ -> 409 Conflict
            if (saleItem.getQuantity() < dto.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Not enough stock for item " + saleItem.getId());
            }

            CartItem cartItem = cartItemRepo.findByCartIdAndSaleItemAndSeller(
                    cart.getId(), saleItem, saleItem.getSeller()
            ).orElse(null);

            if (cartItem != null) {
                cartItem.setQuantity(cartItem.getQuantity() + dto.getQuantity());
                cartItem.setUpdatedOn(Instant.now());
                cartItemRepo.save(cartItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setSaleItem(saleItem);
                newItem.setSeller(saleItem.getSeller());
                newItem.setQuantity(dto.getQuantity());
                newItem.setCreatedOn(Instant.now());
                newItem.setUpdatedOn(Instant.now());
                cartItemRepo.save(newItem);
                cart.getItems().add(newItem);
            }

            // ลด stock
            saleItem.setQuantity(saleItem.getQuantity() - dto.getQuantity());
            saleItemRepo.save(saleItem);
        }

        cart.setUpdatedOn(Instant.now());
        cartRepo.save(cart);

        // Map เป็น Response DTO
        return CartResponseDto.builder()
                .items(cart.getItems().stream().map(ci -> CartItemResponseDto.builder()
                        .saleItemId(ci.getSaleItem().getId())
                        .saleItemName(ci.getSaleItem().getModel())   // ต้องมี field name ใน SaleItemBase
                        .saleItemPrice(ci.getSaleItem().getPrice()) // ต้องมี field price ใน SaleItemBase
                        .quantity(ci.getQuantity())
                        .sellerId(ci.getSeller().getId())
                        .sellerName(ci.getSeller().getFullname())       // ต้องมี field name ใน SellerAccount
                        .build()
                ).toList())
                .build();
    }
}
