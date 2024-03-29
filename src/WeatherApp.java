import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.*;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class WeatherApp extends JFrame{
    private JSONObject weatherData;
    private ImageIcon morningImage;
    private ImageIcon eveningImage;
    public WeatherApp(){
        super("Weather App");
        //load background images when needed based on time
        loadBackgroundImage();
          // Initially set background based on current time
        setBackgroundBasedOnTime(LocalTime.now());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);

        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addGuiComponents();
    }
    public void loadBackgroundImage(){
        try {
            morningImage = new ImageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/morning_image.jpg");
            eveningImage = new ImageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/night_image.jpg");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void setBackgroundBasedOnTime(LocalTime currentTime) {
        int hour = currentTime.getHour();
        if (hour >= 6 && hour < 12) { // Morning
            setContentPane(new JLabel(morningImage));
        }else if (hour >= 18 && hour < 20) { // Evening
            setContentPane(new JLabel(eveningImage));
        }
        revalidate(); // Refresh the frame to reflect the changes
    }
    private void addGuiComponents(){
        JTextField searchField = new JTextField();
        searchField.setBounds(15, 15, 351, 45);
        searchField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(searchField);

        //Add an image to display weather condition
        JLabel weatherConditionImage = new JLabel(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/cloudy.png"));
        weatherConditionImage.setBounds(0, 125, 450, 217);
        add(weatherConditionImage);

        // A label to display temperature in Celcius
        JLabel temperatureText = new JLabel("10 C");
        temperatureText.setBounds(0, 350, 450, 54);
        temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
        temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperatureText);

        // create a label to display weather condition
        JLabel weatherCondition = new JLabel("Cloudy");
        weatherCondition.setBounds(0, 405, 450, 40);
        weatherCondition.setFont(new Font("Dialog", Font.PLAIN, 25));
        weatherCondition.setHorizontalAlignment(SwingConstants.CENTER);
        add(weatherCondition);

        //Add humidity image
        JLabel humidityImage = new JLabel(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/humidity.png"));
        humidityImage.setBounds(15, 500, 74, 66);
        add(humidityImage);

        // Add humidity text
        JLabel humidityText = new JLabel("<html><b> Humidity </b> 100% </html>");
        humidityText.setHorizontalAlignment(SwingConstants.CENTER);
        humidityText.setBounds(90, 500, 85, 55);
        humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(humidityText);

        //Add an image to display wind speed
        JLabel windSpeedImage = new JLabel(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/windspeed.png"));
        windSpeedImage.setBounds(220, 500, 74, 66);
        add(windSpeedImage);

        // Add a text to display windSpeed
        JLabel windSpeedText = new JLabel("<html><b> Windspeed </b> 15km </html>");
        windSpeedText.setBounds(310, 500, 100, 55);
        windSpeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
        add(windSpeedText);

        //Add a search button to our GUI
        JButton searchButton = new JButton(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(375, 13, 50, 50);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = searchField.getText();
                //Validate the user input
                if(userInput.replaceAll("\\s", "").length() <= 0){
                    return;
                }
                weatherData = GLocation.getWeatherData(userInput);
                if(weatherData != null){
                //update our GUI and its weather image
                String weatherCondition = (String) weatherData.get("weather_condition");
                //update the weather image that corresponds with the condition
                switch(weatherCondition){
                    case "Clear":
                        weatherConditionImage.setIcon(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/clear.png"));
                        break;
                    case "Cloudy":
                        weatherConditionImage.setIcon(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/cloudy.png"));
                        break;
                    case "Rainy":
                        weatherConditionImage.setIcon(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/rain.png"));
                        break;
                    case "Snowy":
                        weatherConditionImage.setIcon(imageIcon("/Users/jaloliddin/IdeaProjects/weatherApp/weatherapp_images/snow.png"));
                        break;
                }
                //update temperature text accordingly
                int temperature = (int) Math.round((double) weatherData.get("temperature"));
                temperatureText.setText(temperature + " C");

                //update humidity text
                long humidity = (long) weatherData.get("humidity");
                humidityText.setText("<html><b>Humidity</b> " + humidity +"%</html>");

                //update windspeed text
                double windSpeed = (double) weatherData.get("wind_speed");
                windSpeedText.setText("<html><b>Windspeed</b> <br>" + windSpeed +" km/h </br></html>");


            }else{
                 System.out.println("Failed to fetch data!");
                 // Show error message dialog for failed data fetch
                 JOptionPane.showMessageDialog(WeatherApp.this, "Location data not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(searchButton);

    }
    private ImageIcon imageIcon(String reourcePath){
        try {
            BufferedImage image = ImageIO.read(new File(reourcePath));
            return new ImageIcon(image);

        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Couldn't find the image!");
        return null;
    }
}