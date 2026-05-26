import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Player {

    private List<Item> inventory = new ArrayList<>();
    private int coinsThrown = 0; // coins in the fountain

    public void addItem(Item item) {
        inventory.add(item);
    }

    public boolean hasItem(String name) {
        for (Item item : inventory) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyCoin() {
        for (Item item : inventory) {
            if (item.getName().startsWith("coin")) {
                return true;
            }
        }
        return false;
    }

    public void showInventory() {
        if (inventory.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }

        System.out.println("Inventory:");
        for (Item item : inventory) {
            System.out.println("- " + item.getName());
        }
    }

    public boolean throwCoin() {
        for (Iterator<Item> it = inventory.iterator(); it.hasNext();) {
            Item item = it.next();
            if (item.getName().startsWith("coin")) {
                it.remove();
                coinsThrown++;
                return true;
            }
        }
        return false;
    }

    public int getCoinsThrown() {
        return coinsThrown;
    }

    public List<Item> getInventory() {
        return this.inventory;
    }
}
