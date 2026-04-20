### LOGIN PAGE SELECTORS:
username: id=username
password: id=password
loginButton: name=login

### SHOP PAGE SELECTORS:
allDepartments: xpath=//span[normalize-space()='All departments']
categoryElectronic: xpath=//ul[@id='menu-all-departments-1']//a[contains(@class,'item-link')][normalize-space()='Electronic Components & Supplies']
productCard: css=(//div[contains(@class,'content-product')])[%s]
productName: xpath=(//div[contains(@class,'content-product')])[%s]//h2[contains(@class,'product-title')]
productPrice: xpath=(//div[contains(@class,'content-product')])[%s]//span[contains(@class,'price')]
addToCartBtn: xpath=(//div[contains(@class,'content-product')])[%s]//a[contains(@href,'cart')]
sortDropdown: css=select.orderby
sortPriceAsc: css=select.orderby option[value='price']
sortPriceDesc: css=select.orderby option[value='price-desc']

### SHOPPING CART PAGE SELECTORS:
productName: css=.product-title
productPrice: css=td.product-price span.woocommerce-Price-amount
totalPrice: css=tr.order-total span.woocommerce-Price-amount
quantityTextbox: xpath=//input[contains(@class,'qty') and @type='number']
quantityPlusBtn: css=.plus
quantityMinusBtn: css=.minus
updateCartBtn: css=button[name='update_cart']
clearCartBtn: css=a.et-cart-empty
proceedToCheckout: xpath=//a[contains(normalize-space(),'checkout')]

### CHECKOUT PAGE SELECTORS:
checkoutHeading: xpath=//a[contains(@href,'checkout') and contains(@class,'active')]
billingFirstName: css=#billing_first_name
billingLastName: css=#billing_last_name
billingAddress: css=#billing_address_1
billingCity: css=#billing_city
billingPostcode: css=#billing_postcode
billingPhone: css=#billing_phone
orderProductName: css=td.product-name
orderProductQty: css=.product-quantity
orderTotalPrice: css=tr.order-total span.woocommerce-Price-amount
paymentBankTransfer: css=#payment_method_bacs
paymentCheque: css=#payment_method_cheque
paymentCOD: css=#payment_method_cod
placeOrderBtn: css=#place_order

### ORDER CONFIRMATION PAGE SELECTORS:
statusMessage: css=.woocommerce-notice.woocommerce-thankyou-order-received
orderOverviewSection: css=ul.woocommerce-order-overview.order_details
orderNumber: css=li.woocommerce-order-overview__order > strong
orderDate: css=li.woocommerce-order-overview__date > strong
orderEmail: css=li.woocommerce-order-overview__email > strong
orderTotal: css=li.woocommerce-order-overview__total > strong .woocommerce-Price-amount
orderPaymentMethod: css=li.woocommerce-order-overview__payment-method > strong
orderDetailsSection: css=section.woocommerce-order-details
orderDetailsTable: css=table.woocommerce-table--order-details
orderProductName: css=td.woocommerce-table__product-name.product-name > a
orderProductQty: css=td.woocommerce-table__product-name .product-quantity
orderProductPrice: css=td.woocommerce-table__product-total .woocommerce-Price-amount
orderSubtotal: xpath=//tfoot/tr[th[text()='Subtotal:']]/td//span[@class='woocommerce-Price-amount amount']
orderTotalFooter: xpath=//tfoot/tr[th[text()='Total:']]/td//span[@class='woocommerce-Price-amount amount']
billingSection: css=section.woocommerce-customer-details
billingPhone: css=p.woocommerce-customer-details--phone
billingEmail: css=p.woocommerce-customer-details--email

### ORDER HISTORY PAGE SELECTORS:
ordersNavigation: css=.woocommerce-MyAccount-navigation-link--orders > a
ordersTable: css=table.woocommerce-orders-table
orderRowNumber: css=td.woocommerce-orders-table__cell-order-number a
orderRowStatus: css=td.woocommerce-orders-table__cell-order-status
orderRowViewBtn: css=td.woocommerce-orders-table__cell-order-actions a.view

### PRODUCT REVIEW PAGE SELECTORS:
reviewsTab: css=#tab-reviews
ratingStars: css=.stars a
reviewComment: css=#comment
reviewAuthor: css=#author
reviewEmail: css=#email
submitReviewButton: css=#submit