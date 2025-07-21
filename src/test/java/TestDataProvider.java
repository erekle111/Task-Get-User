import org.testng.annotations.DataProvider;

public class TestDataProvider {

    @DataProvider(name = "getAllUsersData")
    public Object[][] getAllUsersData() {
        return new Object[][]{
                {"/users", 200, 2}
        };
    }

    @DataProvider(name = "filterByAgeData")
    public Object[][] filterByAgeData() {
        return new Object[][]{
                {"/users?age=30", 200, 1, "Alice"},
                {"/users?age=25", 200, 1, "Bob"}
        };
    }

    @DataProvider(name = "filterByGenderData")
    public Object[][] filterByGenderData() {
        return new Object[][]{
                {"/users?gender=male", 200, 1, "Bob"},
                {"/users?gender=female", 200, 1, "Alice"}
        };
    }

    @DataProvider(name = "invalidAgeData")
    public Object[][] invalidAgeData() {
        return new Object[][]{
                {"/users?age=-1", 400}
        };
    }

    @DataProvider(name = "invalidGenderData")
    public Object[][] invalidGenderData() {
        return new Object[][]{
                {"/users?gender=unknown", 422}
        };
    }

    @DataProvider(name = "serverErrorData")
    public Object[][] serverErrorData() {
        return new Object[][]{
                {"/users?error=500", 500}
        };
    }
}