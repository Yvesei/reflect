public class TestClass {
    private String testField;
    private int anotherField;

    public TestClass() {
    }

    public TestClass(String testField, int anotherField) {
        this.testField = testField;
        this.anotherField = anotherField;
    }

    public String getTestField() {
        return testField;
    }

    public int getAnotherField() {
        return anotherField;
    }

    public void setTestField(String testField) {
        this.testField = testField;
    }

    public void setAnotherField(int anotherField) {
        this.anotherField = anotherField;
    }

    public void testMethod() {
        System.out.println("This is a test method.");
    }
}
