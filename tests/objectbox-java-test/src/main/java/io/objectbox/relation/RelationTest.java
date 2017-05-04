package io.objectbox.relation;

import org.junit.Test;

import java.util.List;

import io.objectbox.query.Query;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RelationTest extends AbstractRelationTest {

    @Test
    public void testRelationToOne() {
        Customer customer = putCustomer();
        Order order = putOrder(customer, "Bananas");

        Order order1 = orderBox.get(order.getId());
        assertEquals(customer.getId(), order1.getCustomerId());
        assertNull(order1.peekCustomer());
        assertEquals(customer.getId(), order1.getCustomer().getId());
        assertNotNull(order1.peekCustomer());
    }

    @Test
    public void testRelationToMany() {
        Customer customer = putCustomer();
        Order order1 = putOrder(customer, "Bananas");
        Order order2 = putOrder(customer, "Oranges");

        List<Order> orders = customer.getOrders();
        assertEquals(2, orders.size());
        assertEquals(order1.getId(), orders.get(0).getId());
        assertEquals("Bananas", orders.get(0).getText());
        assertEquals(order2.getId(), orders.get(1).getId());
        assertEquals("Oranges", orders.get(1).getText());
    }

    @Test
    public void testRelationToMany_activeRelationshipChanges() {
        Customer customer = putCustomer();
        Order order1 = putOrder(customer, "Bananas");
        Order order2 = putOrder(customer, "Oranges");

        List<Order> orders = customer.getOrders();
        assertEquals(2, orders.size());
        orderBox.remove(order1);
        ((ToMany) orders).reset();
        assertEquals(1, orders.size());

        order2.setCustomer(null);
        orderBox.put(order2);

        ((ToMany) orders).reset();
        assertEquals(0, orders.size());
    }

    @Test
    public void testAttach() {
        Customer customer = putCustomer();
        putOrder(customer, "Bananas");
        putOrder(customer, "Oranges");

        Customer customerNew = new Customer();
        customerNew.setId(customer.getId());
        customerBox.attach(customerNew);
        assertEquals(2, customerNew.getOrders().size());

        customerNew.setName("Jake");
        customerBox.put(customerNew);
        assertEquals("Jake", customerBox.get(customer.getId()).getName());
    }

    @Test
    public void testRelationToOneQuery() {
        Customer customer = putCustomer();
        Order order = putOrder(customer, "Bananas");

        Query<Order> query = orderBox.query().equal(Order_.customerId, customer.getId()).build();
        Order orderFound = query.findUnique();
        assertEquals(order.getId(), orderFound.getId());
    }

}
