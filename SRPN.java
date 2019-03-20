import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Program class for a SRPN calculator
 *
 * @version 1.0
 * @author Awen Rhys
 */

public class SRPN {

    private Stack<Integer> calcStack = new Stack<>();
    //used in RegEx expressions when splitting strings to keep the delimiter/thing being split by
    static private final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    private boolean inComment = false;
    //the list of numbers the 'r' function loops through, string as they'll be used to call the addNumToStack
    private String[] randomNumbers = {"1804289383", "846930886", "1681692777", "1714636915", "1957747793",
            "424238335", "719885386", "1649760492", "596516649", "1189641421", "1025202362", "1350490027",
            "783368690", "1102520059", "2044897763", "1967513926", "1365180540", "1540383426", "304089172",
            "1303455736", "35005211", "521595368"};
    private int currentRand = 0;

    /**
     * Function to process the input coming in
     *
     * @param s input from the user or called from within the function when input has been split
     **/
    private void processCommand(String s) {
        //checks if there's multiple things to process on this line. This is when separated by spaces
        //or anything other than operations or operands being next to each other
        String[] sections = s.split(String.format(WITH_DELIMITER, "[^-0-9+/^*%-]"));

        //calling this function on each string if there was premise to split the input into multiple strings
        if(sections.length > 1) {
            for (int i = 0; i < sections.length; i++) {
                processCommand(sections[i]);
            }
        } else {
            //checks if it's a # and turns comments on/off - separate here instead of with rest of processing input as
            //the rest only runs if not in comment
            turnOnOffComment(s);

            if (!inComment) {
                if (s.matches("[-]?[0-9]+")) {
                    addNumToStack(s);
                } else if (s.matches("[-+/*%^]")) {
                    doCalculation(s);
                } else if(s.matches("[-+*^%/0-9]+")) {
                    //if it's a mix of operations and operands, process it as single line calculation
                    processLine(s);
                } else if (s.contains("d")) {
                    printStack();
                } else if (s.contains("r")) {
                    addRandNumToStack();
                } else if (s.contains("#") || s.contains(" ")) {
                    //don't need to do anything more with these characters, # already dealt with and spaces ignored
                    //but we don't want them to come up as an unrecognised character
                } else if (s.contains("=")) {
                    printResult();
                } else {
                    //checking length stops it from printing unrecognised when it's a blank line
                    if(s.length() > 0) {
                        System.out.println("Unrecognised operator or operand \"" + s + "\".");
                    }
                }
            }
        }

    }

    /**
     * Function to turn on/off comment mode if a '#' is received
     *
     * @param possibleHash the string passed into the main argument to check if it's a #
     */
    private void turnOnOffComment(String possibleHash) {
        if(possibleHash.contains("#") && inComment) {
            inComment = false;
        } else if(possibleHash.contains("#") && !inComment) {
            inComment = true;
        }
    }

    /**
     * Function to add a number to a stack, converts from octal to decimal
     * if there's a 0 in front of the number
     *
     * @param numString is number to be added to stack, in string so the 0 in front doesn't
     *        get delete and number being over integer max isn't a problem yet
     */
    private void addNumToStack(String numString) {
        if(numString.matches("^[0][0-9]+") || numString.matches("^[-][0][0-9]+")) {
            //checking it only has the digits 0-7 otherwise it isn't a valid octal number
            if(numString.matches("[-]?[0-7]+")) {
                numString = convertToDecimal(numString);
            } else {
                //don't want to add anything to the stack if it isn't an octal number so leaving the function
                //unless it's 08 or 09, which add 8 and 9 resp to the stack
                if(numString.matches("08")) {
                    numString = "8";
                } else if(numString.matches("09")) {
                    numString = "9";
                } else {
                    return;
                }
            }
        }

        //is stack is size 23 which will be the maximum size, print stack overflow
        if (calcStack.size() == 23) {
            System.out.println("Stack overflow.");
        } else {
            //checking with a long to see if it exceeds the integer limits before adding
            if (Long.valueOf(numString) > Integer.MAX_VALUE) {
                calcStack.push(Integer.MAX_VALUE);
            } else if (Long.valueOf(numString) < Integer.MIN_VALUE) {
                calcStack.push(Integer.MIN_VALUE);
            } else {
                //otherwise, adding the number itself to the stack
                calcStack.push(Integer.valueOf(numString));
            }
        }
    }

    /**
     * Function to convert an octal number to a decimal number
     *
     * @param octalNum the octal number to be converted
     * @return returning the decimal number
     */
    private String convertToDecimal(String octalNum) {
        //using a long so it doesn't cause an error when over integer limit
        return Long.toString(Long.parseLong(octalNum, 8));
    }

    private void doCalculation(String operator) {
        //checking there's enough on the stack to pop two items
        if (calcStack.size() >= 2) {
            int y = calcStack.pop();
            int x = calcStack.pop();
            //check variable is used to see if the calculation exceeds integer limit by using a long first
            long check;
            switch (operator) {
                case "+":
                    //casting as long so they add as a long not an integer
                    check = (long) x + (long) y ;
                    //if check shows it's above/under max/min number, add to stack the max/min int possible
                    if (check > Integer.MAX_VALUE) {
                        calcStack.push(Integer.MAX_VALUE);
                    } else if (check < Integer.MIN_VALUE) {
                        calcStack.push(Integer.MIN_VALUE);
                    } else {
                        calcStack.push(x + y);
                    }
                    break;
                case "-":
                    check = (long) x - (long) y;
                    if (check > Integer.MAX_VALUE) {
                        calcStack.push(Integer.MAX_VALUE);
                    } else if (check < Integer.MIN_VALUE) {
                        calcStack.push(Integer.MIN_VALUE);
                    } else {
                        calcStack.push(x - y);
                    }
                    break;
                case "*":
                    check = (long) x * (long) y;
                    if (check > Integer.MAX_VALUE) {
                        calcStack.push(Integer.MAX_VALUE);
                    } else if (check < Integer.MIN_VALUE) {
                        calcStack.push(Integer.MIN_VALUE);
                    } else {
                        calcStack.push(x * y);
                    }
                    break;
                case "/":
                    //ensuring division by 0 isn't possible
                    if(y != 0) {
                        check = (long) x / (long) y;
                        if (check > Integer.MAX_VALUE) {
                            calcStack.push(Integer.MAX_VALUE);
                        } else if (check < Integer.MIN_VALUE) {
                            calcStack.push(Integer.MIN_VALUE);
                        } else {
                            calcStack.push(x / y);
                        }
                    } else {
                        System.out.println("Divide by 0.");
                    }
                    break;
                case "%":
                    check = (long) x % (long) y;
                    if (check > Integer.MAX_VALUE) {
                        calcStack.push(Integer.MAX_VALUE);
                    } else if (check < Integer.MIN_VALUE) {
                        calcStack.push(Integer.MIN_VALUE);
                    } else {
                        calcStack.push(x % y);
                    }
                    break;
                case "^":
                    //ensuring no powers with negative numbers as they will give decimals which can't be
                    //represented through the int values used in the calculator
                    if (y > 0) {
                        check = (long) Math.pow(x, y);
                        if (check > Integer.MAX_VALUE) {
                            calcStack.push(Integer.MAX_VALUE);
                        } else if (check < Integer.MIN_VALUE) {
                            calcStack.push(Integer.MIN_VALUE);
                        } else {
                            calcStack.push((int) Math.pow(x, y));
                        }
                    } else {
                        System.out.println("Negative power.");
                        calcStack.push(x);
                        calcStack.push(y);
                    }
                    break;
            }
        } else {
            //if less than 2 things in the stack
            System.out.println("Stack underflow.");
        }
    }

    /**
     * Function to process a line when many operators/operands are put on a single line with no spaces
     *
     * @param line the line passed into it from main function with all the input
     */
    private void processLine(String line) {
        //calling the function to split a string into operations/operands
        String[] splitString = separateLine(line);

        //dealing with the start one first because otherwise i-1 will cause problems
        if(splitString[0].matches("[-]?[0-9]+")) {
            //if the first one is a number, add to stack
            calcStack.add(Integer.valueOf(splitString[0]));
        } else {
            //otherwise, process the command
            processCommand(splitString[0]);
        }

        for(int i = 1; i < splitString.length; i++) {
            //checking it's not the last one as i + 1 is used
            if(i != splitString.length - 1) {
                //if the current one is an operation, and the next is an operand
                if (splitString[i].matches("[-+*^/%]") && splitString[i + 1].matches("[-]?[0-9]+")) {
                    addNumToStack(splitString[i + 1]);
                    processCommand(splitString[i]);
                    //incrementing i ensures we don't then go through the loop for the operand we've just dealt with
                    i++;

                //if the current one is an operation, and so is the next one, process this operation
                } else if(splitString[i].matches("[-+*^/%]") && splitString[i + 1].matches("[-+*^/&]")) {
                    processCommand(splitString[i]);
                }
            } else {
                //if it's the last one, it has to be an operation otherwise i++ would've skipped over it, so process it
                processCommand(splitString[i]);
            }
        }
    }

    /**
     * Function to separate the line into operations and operands
     *
     * @param line the line to separate
     * @return an array of operations and operands separated
     */
    private String[] separateLine(String line) {
        //splits at each operand, keeping the delimiter so the operands are also in the array
        String[] basicSplit = line.split(String.format(WITH_DELIMITER, "[+-/%*^]"));

        //final split is array list as unsure how many elements it will have, may or may not be less than basicSplit
        ArrayList<String> finalSplit = new ArrayList<>();

        for(int i = 0; i < basicSplit.length; i++) {
            //if there's a - need to inspect more as this could mean a negative number or a subtraction based on context
            if(basicSplit[i].contains("-")) {
                //checking i == 0 case first as it just needs a number next to it to be a negative number
                if(i == 0) {
                    if(basicSplit[1].matches("[0-9]+")) {
                        finalSplit.add("-" + basicSplit[1]);
                        i++;
                    } else {
                        finalSplit.add("-");
                    }

                //if it's right at the end, it's gonna be an operand as no numbers could follow it
                } else if(i == basicSplit.length - 1) {
                    finalSplit.add("-");
                } else {
                    //it's a subtraction if it's between two numbers, or between a number then another operand
                    //it's a minus if it's between an operand then a number
                    if(basicSplit[i-1].matches("[0-9]+") && basicSplit[i+1].matches("[0-9]+")) {
                        finalSplit.add("-");
                    } else if(basicSplit[i-1].matches("[0-9]+") && basicSplit[i+1].matches("[-+/*^%]")) {
                        finalSplit.add("-");
                    } else if(basicSplit[i-1].matches("[-+*/%]") && basicSplit[i+1].matches("[0-9]+")) {
                        finalSplit.add("-" + basicSplit[i+1]);
                        //doing i++ so it doesn't go through the number in the next loop and add it twice
                        i++;
                    }
                }
            } else {
                //if it doesn't contain a -, then the split is already right so add to final split
                finalSplit.add(basicSplit[i]);
            }
        }

        return finalSplit.toArray(new String[0]);
    }

    /**
     * prints the stack, or prints minimum int value if stack is empty
     */
    private void printStack() {
        if (calcStack.size() > 0) {
            //printing out the stack to a string replacing characters so it comes out as a list
            System.out.printf(calcStack.toString().replaceAll("\\[", "")
                    .replaceAll("]", "%n")
                    .replaceAll(", ", "%n"));
        } else {
            //print the min integer is there's an empty stack
            System.out.printf(Integer.MIN_VALUE + "%n");
        }
    }

    /**
     * Adds the current random number from the random array to the stack
     */
    private void addRandNumToStack() {
        addNumToStack(randomNumbers[currentRand]);
        currentRand++;
    }

    /**
     * prints the last thing in the stack as result if there's something in the stack, prints that it's empty if not
     */
    private void printResult() {
        if(calcStack.size() > 0) {
            System.out.println(calcStack.peek());
        } else {
            System.out.println("Stack empty.");
        }
    }

    public static void main(String[] args) {
        SRPN sprn = new SRPN();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            //Keep on accepting input from the command-line
            while(true) {
                String command = reader.readLine();

                //Close on an End-of-file (EOF) (Ctrl-D on the terminal)
                if(command == null)
                {
                    //Exit code 0 for a graceful exit
                    System.exit(0);
                }

                //Otherwise, (attempt to) process the character
                sprn.processCommand(command);
            }
        } catch(IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
