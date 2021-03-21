@startuml
actor Courier
actor Restaurant as Rest
actor User

participant "Customer Service" as Cust
participant "Delivery Service" as Delivery
participant "Order Service"    as Order
participant "Product Service"  as Product
participant "Restaurant Service" as Restaurant
participant "Notification Service" as Notif

User -> Cust : register
Rest -> Product : addProduct
User -> Product : getProducts
User -> Restaurant : getAvailableRestaurants
User -> Order : placeOrder
Order -> Notif : sendNotification
Notif --> Rest : notifyAboutOrder
Rest -> Order : changeStatus (accept/decline)
Order -> Delivery : assignOrder
Delivery -> Notif : sendNotification
Notif --> Courier : notifyAboutAssignment
Courier -> Delivery : pickupOrder
Courier -> Delivery : deliverOrder
Delivery -> Notif : sendNotification
Notif --> Rest : notifyOrderDelivered
Notif --> User : sendInvoice
@enduml