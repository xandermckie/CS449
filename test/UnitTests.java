import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Sprint 0 - demonstrating JUnit testing.
 *
 * <p>These tests verify simple mathematical operations to
 * demonstrate unit testing concepts.
 */
class UnitTests {

    /**
     * User Story: As a developer, I want to ensure that basic arithmetic
     * operations work correctly so that the game logic calculations are accurate.
     *
     * Acceptance Criteria:
     * - Addition should correctly sum two numbers
     * - Subtraction should correctly find the difference
     * - Result should be accurate for both positive and negative numbers
     */
    @Test
    void testBasicArithmetic() {
        Calculator calc = new Calculator();

        // Test addition
        assertEquals(10, calc.add(7, 3),
                "Adding 7 and 3 should equal 10");

        assertEquals(0, calc.add(-5, 5),
                "Adding -5 and 5 should equal 0");

        // Test subtraction
        assertEquals(4, calc.subtract(7, 3),
                "Subtracting 3 from 7 should equal 4");

        assertEquals(-8, calc.subtract(2, 10),
                "Subtracting 10 from 2 should equal -8");
    }

    /**
     * User Story: As a developer, I want to validate input data properly
     * so that the application handles edge cases without crashing.
     *
     * Acceptance Criteria:
     * - Division by zero should throw an exception
     * - Negative numbers should be handled correctly
     * - Valid operations should complete successfully
     */
    @Test
    void testInputValidation() {
        Calculator calc = new Calculator();

        // Test division
        assertEquals(4, calc.divide(12, 3),
                "Dividing 12 by 3 should equal 4");

        // Test division by zero throws exception
        assertThrows(ArithmeticException.class, () -> calc.divide(10, 0),
                "Division by zero should throw ArithmeticException");

        // Test with negative numbers
        assertEquals(-3, calc.divide(9, -3),
                "Dividing 9 by -3 should equal -3");
    }

    /**
     * Simple calculator class for testing purposes.
     */
    static class Calculator {

        /**
         * Adds two integers.
         *
         * @param a first number
         * @param b second number
         * @return sum of a and b
         */
        public int add(int a, int b) {
            return a + b;
        }

        /**
         * Subtracts second number from first.
         *
         * @param a first number
         * @param b second number
         * @return difference of a and b
         */
        public int subtract(int a, int b) {
            return a - b;
        }

        /**
         * Divides first number by second.
         *
         * @param a dividend
         * @param b divisor
         * @return quotient of a divided by b
         * @throws ArithmeticException if b is zero
         */
        public int divide(int a, int b) {
            if (b == 0) {
                throw new ArithmeticException("Cannot divide by zero");
            }
            return a / b;
        }
    }
}