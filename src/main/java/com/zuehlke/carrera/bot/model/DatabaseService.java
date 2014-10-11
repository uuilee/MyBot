package com.zuehlke.carrera.bot.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DatabaseService {
	// mysql_uri = "jdbc:mysql://100.64.2.103:3306";
	// db_name = "cf_461f8527_4ef1_41c2_84e1_732cbae1f235";
	// db_user = "Lwk9ntUUw736PrDG";
	// db_pw = "GbLkYcpT5SLff774";
	private String table_sensor_data = "sensor_data";
	private String table_track = "track";

	private MysqlDataSource dataSource;
	private Connection connectionToDB;
	private Statement statement;
	private ResultSet currentResultSet;

	// mysql_uri should be of the form : jdbc:mysql://ipaddres:port
	public DatabaseService(String mysql_uri, String db_name, String db_user,
			String db_pw) {
		dataSource = new MysqlDataSource();
		dataSource.setURL(mysql_uri + "/" + db_name);
		dataSource.setUser(db_user);
		dataSource.setPassword(db_pw);
	}

	public boolean connectToMariaDB() throws SQLException {
		connectionToDB = dataSource.getConnection();
		return connectionToDB.isValid(0);
	}

	// Should be called with an static lap value which will be used as
	// identifier in the database
	public void insertSensorEvent(SensorEvent data, int currentLap, int power)
			throws SQLException {
		if (statement == null)
			statement = connectionToDB.createStatement();
		float[] acc = new float[3];
		float[] gyro = new float[3];

		statement
				.executeUpdate("INSERT INTO `sensor_data`(`lap`, `timestamp`, `acc_x`, `acc_y`, `acc_z`, `gyro_x`, `gyro_y`, `gyro_z`, `power`) VALUES ("
						+ Integer.toString(currentLap)
						+ ","
						+ Long.toString(data.getTimeStamp())
						+ ","
						+ Float.toString(acc[0])
						+ ","
						+ Float.toString(acc[1])
						+ ","
						+ Float.toString(acc[2])
						+ ","
						+ Float.toString(gyro[0])
						+ ","
						+ Float.toString(gyro[1])
						+ ","
						+ Float.toString(gyro[2])
						+ ","
						+ Integer.toString(power) + ")");
		statement.close();
	}

	// Returns the object id of the track inserted.
	// Should keep it to find the right track object.
	// Pass this as parameter in getLastTrack()
	// If id is -1, something went terribly wrong!
	public int insertTrack(Object object) throws SQLException {
		// Code from following example:
		// http://sanjaal.com/java/252/java-object-serialization/java-object-serialization-and-deserialization-in-mysql-database/
		String className = object.getClass().getName();
		PreparedStatement pstmt = connectionToDB.prepareStatement(
				"INSERT INTO track(object_name, object_value) VALUES(?,?)",
				Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, className);
		pstmt.setObject(2, object);

		pstmt.executeUpdate();
		ResultSet rs = pstmt.getGeneratedKeys();
		int id = -1;
		if (rs.next()) {
			id = rs.getInt(1);
		}
		rs.close();
		pstmt.close();
		return id;
	}

	public Object getLastTrack(int id) throws SQLException, IOException,
			ClassNotFoundException {

		// Code from following example:
		// http://sanjaal.com/java/252/java-object-serialization/java-object-serialization-and-deserialization-in-mysql-database/
		PreparedStatement pstmt = connectionToDB
				.prepareStatement("SELECT object_value FROM track WHERE id = ?");
		pstmt.setLong(1, id);
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		byte[] buf = rs.getBytes("object_value");
		ObjectInputStream objectIn = null;
		if (buf != null)
			objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
		Object object = objectIn.readObject();
		rs.close();
		pstmt.close();
		return object;
	}

	// Return null if there was no data in database found
	public ArrayList<SensorEvent> getLapEvents(int lapIndex)
			throws SQLException {
		SensorEvent currentEvent;
		float[] acc = new float[3];
		float[] gyro = new float[3];
		float[] dummy_mag = { 0.0f, 0.0f, 0.0f };
		long timeStamp = -1;
		ArrayList<SensorEvent> lapEvents = new ArrayList<SensorEvent>();
		if (statement == null) {
			statement = connectionToDB.createStatement();
		}
		currentResultSet = statement.executeQuery("SELECT * FROM "
				+ table_sensor_data + " WHERE lap ="
				+ Integer.toString(lapIndex));
		// maybe if correct lap got fetched
		currentResultSet.first();
		while (!currentResultSet.isAfterLast()) {
			acc[0] = currentResultSet.getFloat("acc_x");
			acc[1] = currentResultSet.getFloat("acc_y");
			acc[2] = currentResultSet.getFloat("acc_z");
			gyro[0] = currentResultSet.getFloat("gyro_x");
			gyro[1] = currentResultSet.getFloat("gyro_y");
			gyro[2] = currentResultSet.getFloat("gyro_z");
			timeStamp = currentResultSet.getLong("timestamp");

			currentEvent = new SensorEvent(acc, gyro, dummy_mag, timeStamp);
			lapEvents.add(currentEvent);
			currentResultSet.next();
		}
		currentResultSet.close();
		statement.close();
		return lapEvents;
	}

	public ArrayList<SensorEvent> getSortedLapEvents(int roundIndex)
			throws SQLException {
		ArrayList<SensorEvent> lapEvents = new ArrayList<SensorEvent>();

		// todo get lap events and built the array
		return lapEvents;
	}

	// Call in a finally block to ensure closing the connection to the database
	public void closeConnection() throws SQLException {
		connectionToDB.close();
	}

}
