import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class GameLogic {
    private Player player = new Player();
    private Room currentRoom;

    private Room fountain, gate;
    private Room shed, graveyard, cellar;
    private Room hedgeFront, cellarFront;

    private boolean catDiscovered = false;
    private boolean walletReturned = false;
    private boolean shedOpened = false;
    private boolean scissorsReceived = false;
    private boolean cellarVisited = false;
    private boolean screwdriverReceived = false;
    private boolean poisonReceived = false;
    private boolean gasMaskReceived = false;
    private boolean poisonUsed = false;
    private boolean talkingToCat = false;
    private boolean keyReceived = false;
    private boolean ventOpened = false;
    private boolean hedgeCut = false;

    private int coinsThrown = 0;

    public GameLogic() {
        createRooms();
        currentRoom = fountain;
    }

    private void createRooms() {
        fountain = new Room("FOUNTAIN", new String[]{
                "Rain splatters on the surface.",
                "The water ripples unnaturally.",
                "The silence around me feels suffocating."
        }, "fountainBlue", 0, -30);

        gate = new Room("GATE", new String[]{
                "This is your way out of here. This is freedom."
        }, "reset", 0, -150);

        shed = new Room("SHED", new String[]{
                "You hear rattling inside.",
                "The old building creaks and wobbles in the wind.",
                "You can see a pair of yellow eyes glaring at you through the window."
        }, "shedBrown", 0, 105);

        cellarFront = new Room("CELLAR DOORS", new String[]{
                "It's an old cellar door. The lock is rusty and weathered."
        }, "cellarRock", 195, -30);

        hedgeFront = new Room("TANGLED HEDGE", new String[]{
                "Nature has run it's course."
        }, "graveyardGrey", -100, -30);


        cellar = new Room("CELLAR", new String[]{
                "Cold air surrounds you. It's damp and musty.",
                "There's a sharp chemical smell in the air.",
                "Water drips slowly from the cracks in the old stone walls."
        }, "cellarRock", 250, -30);

        graveyard = new Room("GRAVEYARD", new String[]{
                "The lilies are deadly silent. Somehow it feels like they're trying to warn you.",
                "They all look like you. You are one of them.",
                "Something is wrong here. The air feels hard to breathe."
        }, "graveyardGrey", -185, -30);

        fountain.setNorth(gate);
        fountain.setSouth(shed);
        fountain.setEast(cellarFront);
        fountain.setWest(hedgeFront);

        gate.setSouth(fountain);

        shed.setNorth(fountain);

        cellarFront.setWest(fountain);
        cellarFront.setEast(cellar);
        cellar.setWest(cellarFront);

        hedgeFront.setEast(fountain);
        hedgeFront.setWest(graveyard);
        graveyard.setEast(hedgeFront);
    }

    public String move(String direction) {
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) return "You can't go that way.";

        if (nextRoom == graveyard && !player.hasItem("scissors")) {
            return "The hedge is too thick to get through.";
        } else if (nextRoom == graveyard && player.hasItem("scissors") && !hedgeCut) {
            currentRoom = nextRoom;
            hedgeCut = true;
            return "HEDGE_CUT_SUCCESS";
        }

        if (nextRoom == cellar && !player.hasItem("cellar key") && !cellarVisited) {
            return "The cellar is locked. You need to find a key.";
        }
        else if (nextRoom == cellar && player.hasItem("cellar key") && !cellarVisited) {
            currentRoom = nextRoom;
            cellarVisited = true;
            return "CELLAR_UNLOCKED";
        }

        currentRoom = nextRoom;

        if (currentRoom == gate) {
            if (!keyReceived) {
                return "GATE_LOCKED";
            } else {
                return "GATE_CHOICE";
            }
        }

        return "ROOM_CHANGED";
    }

    public String[] getLookChoices() {
        if (currentRoom == fountain) {
            return new String[]{
                    "The gate to the north",
                    "The shed to the south",
                    "The door to the west",
                    "The path hedge to the east",
                    "Interact with the fountain"
            };
        }
        if (currentRoom == shed) {
            if (talkingToCat) {
                return new String[]{
                        "Ask about escaping the garden",
                        "Ask for help with items",
                        "Return wallet",
                        "Leave the cat alone"
                };
            }
            if (shedOpened) {
                return new String[]{
                        "Check the door",
                        "Look behind the shed",
                        "Look under the floormat",
                        "Look at the roof",
                        "Talk to cat"
                };
            }
            return new String[]{
                    "Check the door",
                    "Look behind the shed",
                    "Look under the floormat",
                    "Look at the roof"
            };
        }
        if (currentRoom == graveyard) {
            return new String[]{
                    "Walk through the misty field of lilies",
                    "Break off a branch from the lonely tree",
                    "Inspect the lonely tree",
                    "Inspect a suspicious dirt patch behind the tree",
                    "Inspect the field center air vent"
            };
        }
        if (currentRoom == cellar) {
            return new String[]{
                    "Go to the vent in the back of the room",
                    "Inspect the stack of boxes",
                    "Look at the sign on the wall",
                    "Reach up to the shelf"
            };
        }
        return new String[0];
    }

    public String[] executeMenuAction(int index) {
        String roomName = currentRoom.getName().toUpperCase();

        if (roomName.equals("FOUNTAIN")) {
            if (index == 0) {
                return new String[]{
                        "The gate leads out of the garden.",
                        "You look up and see a castle on the cliffside.",
                        "That's where she is. The Queen. The fake. The tyrant."
                };
            }
            if (index == 1) {
                return new String[]{
                        "You hear rattling from the shed. Something is moving in there.",
                        "You might want to take a closer look."
                };
            }
            if (index == 2) {
                return new String[]{"It's an old cellar door. The lock is rusty and weathered."};
            }
            if (index == 3) {
                return new String[]{"The overgrown hedge is blocking your path."};
            }
            if (index == 4) {
                if (player.hasAnyCoin()) {
                    if (player.throwCoin()) {
                        int c = player.getCoinsThrown();

                        String firstLine = "You throw a coin into the fountain.";
                        String secondLine = "";

                        if (c == 1) {
                            secondLine = "You feel a bit luckier.";
                        } else if (c == 2) {
                            secondLine = "Luck is on your side.";
                        } else {
                            secondLine = "You make your own luck.";
                        }
                        return new String[]{ firstLine, secondLine };
                    }
                } else {
                    return new String[]{"A fountain. I wonder if it's lucky? Maybe I can find a coin?"};
                }
            }
        }


        if (roomName.contains("SHED")) {
            if (talkingToCat) {
                if (index == 0) { // Ask about escaping
                    if (!walletReturned) {
                        return new String[]{"\"Find my wallet first. I had to climb over a hedge to get here.\""};
                    } else {
                        keyReceived = true;
                        if (!player.hasItem("gate key")) {
                            player.addItem(new Item("gate key", "The key to escaping."));
                        }

                        int coins = player.getCoinsThrown();

                        // Custom 0 Coin Branch Text
                        if (coins == 0) {
                            return new String[]{
                                    "\"What a shame. Although I must say, I am not surprised. You are a lily after all. You didn't even drop a single piece of copper into the waters, did you?\"",
                                    "The cat sighs deeply and drops a heavy, cold object into your hand.",
                                    "You received the GATE KEY!",
                                    "[GATE KEY has been added to your inventory!]"
                            };
                        }
                        // Custom 1 Coin Branch Text
                        else if (coins == 1) {
                            return new String[]{
                                    "\"I guess it's no good to put your faith in others. You'll always be disappointed. Still, you tested your luck once...\"",
                                    "The cat slips an old iron key through the door gap.",
                                    "You received the GATE KEY!",
                                    "[GATE KEY has been added to your inventory!]"
                            };
                        }
                        // Custom 2 Coin Branch Text
                        else if (coins == 2) {
                            poisonReceived = true;
                            if (!player.hasItem("poison")) {
                                player.addItem(new Item("poison", "Strange poison"));
                            }
                            return new String[]{
                                    "\"Ah, you've spent quite a bit of time at the fountain, haven't you? Sure, here's the key...but there's more...if you have the guts.\"",
                                    "You received the GATE KEY!",
                                    "[GATE KEY has been added to your inventory!]",
                                    "\"I feel like I can trust you. You can end this nightmare. The vents are connected to the castle, did you know? Leave the poison near a vent in a place with poor airflow and soon the Queen will fall, along with anyone else in this forsaken kingdom. Although...I feel I must warn you, you will not have time to leave.\"",
                                    "[POISON has been added to your inventory!]"
                            };
                        }
                        // Custom 3+ Coins Branch Text (True Ending Setup)
                        else {
                            poisonReceived = true;
                            if (!player.hasItem("poison")) {
                                player.addItem(new Item("poison", "Strange poison"));
                            }
                            if (!gasMaskReceived) {
                                gasMaskReceived = true;
                                player.addItem(new Item("gas mask", "An old dusty gas mask."));
                            }
                            return new String[]{
                                    "\"You... you're not as bad as the other it seems. Or maybe you're just...lucky? Hmmm...\"",
                                    "You received the GATE KEY!",
                                    "[GATE KEY has been added to your inventory!]",
                                    "\"Take this poison. Set it near a vent in a poorly ventilated place and you'll make things right. Normally, the fumes would end you as well...\"",
                                    "[POISON has been added to your inventory!]",
                                    "The cat reaches back into the shadows and pulls out a heavy, dusty mask-like object.",
                                    "\"...But your kindness has earned you this. Wear it. Sabotage her system, and run for the iron gates. You will survive. At least for today.\"",
                                    "[GAS MASK has been added to your inventory!]"
                            };
                        }
                    }
                }
                if (index == 1) { // Item Help
                    boolean gaveSomething = false;
                    String responseText = "";
                    String alertText = "";

                    if (!scissorsReceived) {
                        player.addItem(new Item("scissors", "Garden scissors"));
                        scissorsReceived = true;
                        gaveSomething = true;
                        responseText = "\"Oh you need scissors? I wonder what for?\"";
                        alertText = "[GARDEN SCISSORS have been added to your inventory!]";
                    } else if (!screwdriverReceived && cellarVisited) {
                        player.addItem(new Item("screwdriver", "Rusty screwdriver"));
                        screwdriverReceived = true;
                        gaveSomething = true;
                        responseText = "\"Anything else you need? Perhaps an arm and a leg?\"";
                        alertText = "[RUSTY SCREWDRIVER have been added to your inventory!]";
                    }

                    if (!gaveSomething) {
                        return new String[]{"\"I've given you everything I have, little lily. Don't be greedy.\""};
                    }
                    return new String[]{responseText, alertText};
                }
                if (index == 2) {
                    if (player.hasItem("wallet") && !walletReturned) {
                        walletReturned = true;
                        return new String[]{"\"Ah, my wallet! Thank you. Now, how can I help?\""};
                    }
                    return new String[]{"\"You don't have anything that belongs to me.\""};
                }
                if (index == 3) {
                    talkingToCat = false;
                    return new String[]{"You step away from the door."};
                }
            }
            else {
                if (index == 0) {
                    if (!catDiscovered) {
                        catDiscovered = true;
                        return new String[]{
                                "\"Who's there? Who are you?\"",
                                "\"Ah, you're one of them...That's not ideal. Never mind.\"",
                                "\"Listen up! I need your help.\"",
                                "\"I came in here to get shelter from the rainstorm but the door closed behind me and now I'm stuck!\"",
                                "\"A key, A KEY, you need to find a key! Look around, it must be in this garden.\""
                        };
                    } else if (player.hasItem("shed key") && !shedOpened) {
                        shedOpened = true;
                        return new String[]{"You have the shed key. You open the door. The cat is waiting for you."};
                    } else if (shedOpened) {
                        return new String[]{"The heavy timber door is wide open. The cat is watching you from inside."};
                    } else {
                        return new String[]{"\"Stop knocking! It's not helping! Find the key!\""};
                    }
                }
                if (index == 1) {
                    return new String[]{
                            "You find boxes full of some sort of chemical. Fertilizer?",
                            "The labels aren't legible because of the rain."
                    };
                }
                if (index == 2) {
                    if (!player.hasItem("shed key")) {
                        player.addItem(new Item("shed key", "Key for the shed."));
                        return new String[]{
                                "You lift up the heavy, wet doormat.",
                                "You found a key.",
                                "[SHED KEY has been added to your inventory!]"
                        };
                    }
                    return new String[]{"There is nothing left under the mat."};
                }
                if (index == 3) {
                    if (player.hasItem("stick") && !player.hasItem("coin1")) {
                        player.addItem(new Item("coin1", "A shiny coin from the roof."));
                        return new String[]{
                                "Something is glinting up there. You can't reach it.",
                                "You use the long tree branch to poke at the roof...",
                                "A coin falls down!",
                                "[COIN has been added to your inventory!]"
                        };
                    } else if (player.hasItem("coin1")) {
                        return new String[]{"There is nothing else on the roof."};
                    } else {
                        return new String[]{
                                "Something is glinting up there. You can't reach it.",
                                "It's too high. You need something to knock it down."
                        };
                    }
                }
                if (index == 4 && shedOpened) { // Open Cat Sub-Menu Tree
                    talkingToCat = true;
                    return new String[]{"You step closer. The cat looks at you in an unsettling manner."};
                }
            }
        }

        if (roomName.equals("GRAVEYARD")) {
            if (index == 0) {
                if (!player.hasItem("wallet")) {
                    player.addItem(new Item("wallet", "A leather wallet"));
                    return new String[]{
                            "You pass by countless of entranced lilies. Something stops you.",
                            "You found a wallet.",
                            "[LEATHER WALLET has been added to your inventory!]"
                    };
                }
                return new String[]{"You see only swaying lilies now."};
            }
            if (index == 1) {
                if (!player.hasItem("stick")) {
                    player.addItem(new Item("stick", "Tree branch"));
                    return new String[]{
                            "You look up at the lonely tree.",
                            "You break a branch.",
                            "[STICK has been added to your inventory!]"
                    };
                }
                return new String[]{"You have already broken a branch."};
            }
            if (index == 2) {
                if (player.hasItem("stick") && !player.hasItem("cellar key")) {
                    player.addItem(new Item("cellar key", "Rusty key"));
                    return new String[]{
                            "There's a rusty key hanging on a branch.",
                            "You have a stick. You can reach the key.",
                            "[CELLAR KEY has been added to your inventory!]"
                    };
                } else if (player.hasItem("cellar key")) {
                    return new String[]{"The branch where the key was is now empty."};
                } else {
                    return new String[]{
                            "There's a rusty key hanging on a branch.",
                            "It's too high. You need something to knock it down."
                    };
                }
            }
            if (index == 3) {
                if (player.hasItem("shovel") && !player.hasItem("coin2")) {
                    player.addItem(new Item("coin2", "Buried coin"));
                    return new String[]{
                            "There's a suspicious patch of dirt here.",
                            "You have a shovel. You can dig up what was buried.",
                            "You find a coin!",
                            "[COIN has been added to your inventory!]"
                    };
                } else if (!player.hasItem("shovel")) {
                    return new String[]{
                            "There's a suspicious patch of dirt here.",
                            "You need something to dig with."
                    };
                } else {
                    return new String[]{"The dirt has already been disturbed."};
                }
            }
            if (index == 4) {
                return new String[]{"It's a vent. Must be part of a larger system."};
            }
        }

        if (roomName.contains("CELLAR")) {
            if (index == 0) {
                if (!player.hasItem("screwdriver") && !ventOpened) {
                    return new String[]{"You could remove the vent cover with the right tool."};
                }
                else if (poisonReceived && player.hasItem("gas mask") && ventOpened) {
                    poisonUsed = true;
                    return new String[]{
                            "You set the poison in the ventilation shaft and uncap the lid.",
                            "A cloud of purple smoke billows out of the vent.",
                            "Quickly, you pull the straps of the mask over your face.",
                            "The air is toxic now, but your mask is doing it's job. You need to run to the gate to escape before it's too late!"
                    };
                }
                else if (poisonReceived && ventOpened) {
                    poisonUsed = true;
                    saveEndingToFile("ENDING 4: Tyranny Overthrown");
                    return new String[]{
                            "You put the poison into the vent and uncap the lid.",
                            "You sit down on the cold cellar floor.",
                            "You feel at peace.",
                            "==== ENDING: You ended the tyranny. ===="
                    };
                } else if (player.hasItem("screwdriver")) {
                    ventOpened = true;
                    if (!player.hasItem("coin3")) {
                        player.addItem(new Item("coin3", "A coin hidden behind a vent cover."));
                        return new String[]{
                                "You have a screwdriver. You remove the vent cover.",
                                "You reach into the dark vent. Your hand touches something cold. It's a coin!",
                                "[COIN has been added to your inventory!]"
                        };
                    }
                    return new String[]{"The vent is empty."};
                }
            }
            if (index == 1) {
                return new String[]{"There's boxes of chemicals here. The label says 'Sedative for soil'. CAUTION! DO NOT INHALE!"};
            }
            if (index == 2) {
                return new String[]{"WARNING! Clean vents monthly. Sedative from soil can become airborne and cause destruction if vents are not maintained properly."};
            }
            if (index == 3) {
                if (!player.hasItem("shovel")) {
                    player.addItem(new Item("shovel", "Small shovel"));
                    return new String[]{
                            "There's a small shovel up there.",
                            "[SMALL SHOVEL has been added to your inventory!]"
                    };
                }
                return new String[]{"The shelf is empty now."};
            }
        }

        return new String[]{"Nothing notable happens."};
    }

    private void saveEndingToFile(String endingName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("achievements.txt", true))) {
            writer.write(endingName);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Could not save achievement: " + e.getMessage());
        }
    }

    public Room getCurrentRoom() { return currentRoom; }
    public Player getPlayer() { return player; }
    public boolean isHedgeCut() { return hedgeCut; }
}