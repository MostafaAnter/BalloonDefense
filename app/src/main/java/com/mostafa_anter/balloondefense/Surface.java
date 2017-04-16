package com.mostafa_anter.balloondefense;

import java.util.ArrayList;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.*;

public class Surface extends SurfaceView implements Runnable, OnTouchListener, OnClickListener, OnLongClickListener, SensorEventListener {

	//game loop
	boolean isRunning = false, initialised = false;
	SurfaceHolder ourholder;
	Thread thread = null;
	int width = -1;
	int height = -1;
	Context context;
	Activity activity;
	long laststep;

	//sensor
	SensorManager sm;
	Sensor s;
	float sensorx, calibratex = 0;
	float sensory, calibratey = 0;
	boolean default_lanscape = false;

	//general images
	gif myplane = null;
	Bitmap cloud_img = null, smoke_img = null;
	Bitmap play_img, settings_img, highscores_img, exit_img, home_img, selected_home_img, selected_play_img, selected_settings_img, selected_highscores_img, selected_exit_img;
	Bitmap speed_img, selected_speed_img, pause_img, pause_play_img;
	Bitmap background_img;
	Bitmap sound_img, music_img, sound_mute_img, music_mute_img, boost_instruction_img, instruction_img;

	//plane
	float planex = 50, planey, speedy;

	//obstacles
	ArrayList<Integer> obstaclex = new ArrayList<Integer>();
	ArrayList<Integer> obstacley = new ArrayList<Integer>();
	ArrayList<Integer> obstacletype = new ArrayList<Integer>();
	//----------------------------------------------------------------------------------------CHANGE ME 0 (remember to change text in strings.xml)
	//----------------------------------------------------------------------------------------CHANGE ME 1 (change obstacles)
	Bitmap[] obstacle_img = new Bitmap[6];//obstacle number
	final int balloon1 = 0, balloon2 = 1, balloon3 = 2, tower1 = 3, tower2 = 4, bomb = 5;//obstacle names
	Boolean[] groundobstacle = new Boolean[] { false, false, false, true, true, false };//is obstacle on ground? (like a tower)
	int[] obstacle_points = new int[] { 2, 2, 5, -1, -1, -1 };//obstacle points. -1 for game over.
	int[] obstacle_speed = new int[] { 2, 4, 8, 8, 8, 7 };//obstacle speed

	//game play
	int score = 0;
	int time = 0;
	final int menu = 0, play = 1, hiscores = 3, gameover = 4, settings = 5;
	int state = menu;
	boolean paused = false;
	boolean levelstarted = false, instructions = false;
	long now = SystemClock.uptimeMillis();
	long start;
	int countdown;
	float backgroundx1 = 0, backgroundx2 = 0;

	//paints
	Paint scoretext = new Paint();
	Paint stroke_scoretext = new Paint();
	Paint logotext = new Paint();
	Paint titletext = new Paint();
	Paint stroke_titletext = new Paint();
	Paint menutext = new Paint();
	Paint gameplaytext = new Paint();
	Paint semitrasparentwhite = new Paint();
	Paint trasparentblack = new Paint();
	Paint background = new Paint();
	Paint menu_background = new Paint();
	Paint white = new Paint();
	Paint black = new Paint();
	Paint yellow_light = new Paint();
	Paint blue = new Paint();

	//clouds
	Particles cloud1, cloud2, smoke;

	//score
	int[] hiscore = new int[10];
	String[] hiscorename = new String[10];

	//graphics
	boolean oncalibrate = false, onplay = false, onsettings = false, onexit = false, onhighscores = false, onmenu = false, onspeed = false;

	//sound
	SoundPool sp;
	int sound_score, sound_beep;
	MediaPlayer music;
	boolean sound_muted = false, music_muted = false;

	public Surface(Context context, Activity activity) {
		super(context);
		//listeners
		setOnTouchListener(this);
		setOnClickListener(this);
		setOnLongClickListener(this);

		//sensors
		sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sm.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
			s = sm.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
			sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
		}

		//load images-----------------------------------------------------------------------CHANGE ME 2 (change image names if you added obstacles or changed image names)
		//obstacle images
		obstacle_img[0] = BitmapFactory.decodeResource(getResources(), R.drawable.balloon1);
		obstacle_img[1] = BitmapFactory.decodeResource(getResources(), R.drawable.balloon2);
		obstacle_img[2] = BitmapFactory.decodeResource(getResources(), R.drawable.balloon3);
		obstacle_img[3] = BitmapFactory.decodeResource(getResources(), R.drawable.tower);
		obstacle_img[4] = BitmapFactory.decodeResource(getResources(), R.drawable.tower2);
		obstacle_img[5] = BitmapFactory.decodeResource(getResources(), R.drawable.bomb);

		//particles images
		cloud_img = BitmapFactory.decodeResource(getResources(), R.drawable.cloud);

		//menu images
		play_img = BitmapFactory.decodeResource(getResources(), R.drawable.play);
		settings_img = BitmapFactory.decodeResource(getResources(), R.drawable.settings);
		highscores_img = BitmapFactory.decodeResource(getResources(), R.drawable.highscores);
		exit_img = BitmapFactory.decodeResource(getResources(), R.drawable.exit);
		home_img = BitmapFactory.decodeResource(getResources(), R.drawable.home);
		pause_img = BitmapFactory.decodeResource(getResources(), R.drawable.pause);
		pause_play_img = BitmapFactory.decodeResource(getResources(), R.drawable.pause_play);
		speed_img = BitmapFactory.decodeResource(getResources(), R.drawable.speed);
		background_img = BitmapFactory.decodeResource(getResources(), R.drawable.background);
		boost_instruction_img = BitmapFactory.decodeResource(getResources(), R.drawable.boost_instruction);
		instruction_img = BitmapFactory.decodeResource(getResources(), R.drawable.instruction);

		//sound images
		sound_img = BitmapFactory.decodeResource(getResources(), R.drawable.sound);
		sound_mute_img = BitmapFactory.decodeResource(getResources(), R.drawable.sound_mute);
		music_img = BitmapFactory.decodeResource(getResources(), R.drawable.music);
		music_mute_img = BitmapFactory.decodeResource(getResources(), R.drawable.music_mute);

		//sound
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		sound_score = sp.load(activity, R.raw.score, 1);
		sound_beep = sp.load(activity, R.raw.beep, 1);
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		music = MediaPlayer.create(activity, R.raw.music);

		//loop
		ourholder = getHolder();
		thread = new Thread(this);
		thread.start();

		//high score restart
		for (int i = 0; i < 10; i++) {
			hiscore[i] = 0;
			hiscorename[i] = "---";
		}

		this.context = context;
		this.activity = activity;

	}

	//inputs///////////////////////////////////////////////////////////////////////////////////////////////////
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		//sleep for fps
		try {
			Thread.sleep(16);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		//read values

		if (default_lanscape) {
			sensorx = event.values[1];
			sensory = -event.values[0];
		} else {
			sensory = event.values[1];
			sensorx = event.values[0];
		}

	}

	public boolean onLongClick(View arg0) {
		return false;
	}

	public void onClick(View v) {
	}

	public boolean onTouch(View v, MotionEvent event) {
		//sleep for fps
		try {
			Thread.sleep(50);//20fps
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		float touchx = event.getX();
		float touchy = event.getY();

		//screen released
		if (event.getAction() == MotionEvent.ACTION_UP) {
			//release all keys
			oncalibrate = false;
			onplay = false;
			onsettings = false;
			onexit = false;
			onhighscores = false;
			onmenu = false;
			onspeed = false;

			if (state == settings) {
				//calibrate
				if (intersect((width / 2) - (width / 4), (height / 2) - (width / 20), (width / 2), (width / 10), (int) touchx, (int) touchy)) {
					calibratex = sensorx;
					calibratey = sensory;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					state = menu;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
			} else if (state == hiscores) {
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					state = menu;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
			} else if (state == menu) {
				//play
				if (intersect((int) ((width / 2.4f) - (play_img.getWidth() / 2)), (int) ((height / 1.5f) - (play_img.getHeight() / 2)), play_img.getWidth(), play_img.getHeight(), (int) touchx, (int) touchy)) {
					for (int i = 0; i <= obstaclex.size() - 1; i++) {
						//delete all
						obstaclex.remove(i);
						obstacley.remove(i);
						obstacletype.remove(i);
					}

					start = SystemClock.uptimeMillis();
					levelstarted = false;
					planey = height / 2;
					myplane.rotate(0);
					instructions = true;
					score = 0;
					time = 0;

					state = play;
					if (!music_muted) {
						music = MediaPlayer.create(activity, R.raw.music);
						music.start();
						music.setLooping(true);
					}
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);

				}
				//highscores
				if (intersect((int) ((width * 0.70f) - (highscores_img.getWidth() / 2)), (int) ((height * 0.4f) - (highscores_img.getHeight() / 2)), highscores_img.getWidth(), highscores_img.getHeight(), (int) touchx, (int) touchy)) {
					state = hiscores;
					loadscore();
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
				//settings
				if (intersect((int) ((width / 4f) - (settings_img.getWidth() / 2)), (int) ((height / 2.2f) - (settings_img.getHeight() / 2)), settings_img.getWidth(), settings_img.getHeight(), (int) touchx, (int) touchy)) {
					state = settings;
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}
				//exit
				if (intersect((int) ((width * 0.9f) - (exit_img.getWidth() / 2)), (int) ((height * 0.9f) - (exit_img.getHeight() / 2)), exit_img.getWidth(), exit_img.getHeight(), (int) touchx, (int) touchy)) {
					System.exit(0);
					activity.finish();
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
				}

			} else if (state == play) {
				if (instructions) {
					if (sound_beep != 0 && !sound_muted)
						sp.play(sound_beep, 1, 1, 0, 0, 0);
					instructions = false;
					start = SystemClock.uptimeMillis();
				} else {

					if (levelstarted) {
						if (intersect((width / 2) - (pause_img.getWidth() / 2), (int) (pause_img.getHeight() * 0.2f), pause_img.getWidth(), pause_img.getHeight(), (int) touchx, (int) touchy)) {
							if (paused) {
								paused = false;
								if (!music_muted) {
									music = MediaPlayer.create(activity, R.raw.music);
									music.start();
									music.setLooping(true);
								}
							} else {
								paused = true;
								music.stop();
							}
							if (sound_beep != 0 && !sound_muted)
								sp.play(sound_beep, 1, 1, 0, 0, 0);
						} else {
							if (paused) {
								if (sound_beep != 0 && !sound_muted)
									sp.play(sound_beep, 1, 1, 0, 0, 0);
								paused = false;
								if (!music_muted) {
									music = MediaPlayer.create(activity, R.raw.music);
									music.start();
									music.setLooping(true);
								}
							}
						}
					}
				}

			} else if (state == gameover) {
				show_enter_highscore();
				if (sound_beep != 0 && !sound_muted)
					sp.play(sound_beep, 1, 1, 0, 0, 0);
			}
		}

		//screen touched
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (state == settings) {
				//calibrate
				if (intersect((width / 2) - (width / 4), (height / 2) - (width / 20), (width / 2), (width / 10), (int) touchx, (int) touchy)) {
					oncalibrate = true;
				}
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					onmenu = true;
				}
			} else if (state == hiscores) {
				//back to main menu
				if (intersect((int) ((width * 0.9f) - (home_img.getWidth() / 2)), (int) ((height * 0.9f) - (home_img.getHeight() / 2)), home_img.getWidth(), home_img.getHeight(), (int) touchx, (int) touchy)) {
					onmenu = true;
				}
			} else if (state == menu) {
				//play
				if (intersect((int) ((width / 2.4f) - (play_img.getWidth() / 2)), (int) ((height / 1.5f) - (play_img.getHeight() / 2)), play_img.getWidth(), play_img.getHeight(), (int) touchx, (int) touchy)) {
					onplay = true;
				}
				//highscores
				if (intersect((int) ((width * 0.70f) - (highscores_img.getWidth() / 2)), (int) ((height * 0.4f) - (highscores_img.getHeight() / 2)), highscores_img.getWidth(), highscores_img.getHeight(), (int) touchx, (int) touchy)) {
					onhighscores = true;
				}
				//settings
				if (intersect((int) ((width / 4f) - (settings_img.getWidth() / 2)), (int) ((height / 2.2f) - (settings_img.getHeight() / 2)), settings_img.getWidth(), settings_img.getHeight(), (int) touchx, (int) touchy)) {
					onsettings = true;
				}
				//exit
				if (intersect((int) ((width * 0.9f) - (exit_img.getWidth() / 2)), (int) ((height * 0.9f) - (exit_img.getHeight() / 2)), exit_img.getWidth(), exit_img.getHeight(), (int) touchx, (int) touchy)) {
					onexit = true;
				}

			} else if (state == play) {
				if (intersect((int) (width * 0.92f - (speed_img.getWidth()/2)) , (int) (height* 0.88f - (speed_img.getHeight()/2)), speed_img.getWidth(), speed_img.getHeight(), (int) touchx, (int) touchy)) {
					onspeed = true;
				}

				//Play screen down
			} else if (state == gameover) {
				//gameover screen down
			}

			if (state == play || state == menu) {
				if (intersect(((int) ((width * 0.95f) - (music_mute_img.getWidth() / 2))), (int) (music_mute_img.getHeight() * 0.12f), music_mute_img.getWidth(), music_mute_img.getHeight(), (int) touchx, (int) touchy)) {
					if (music_muted) {
						music_muted = false;
						if (state == play && !music.isPlaying()) {
							music = MediaPlayer.create(activity, R.raw.music);
							music.start();
							music.setLooping(true);
						}
					} else {
						music_muted = true;
						music.stop();

					}
				}
				if (intersect(((int) (width * 0.88f) - (sound_mute_img.getWidth() / 2)), (int) (sound_mute_img.getHeight() * 0.12f), sound_mute_img.getWidth(), sound_mute_img.getHeight(), (int) touchx, (int) touchy)) {
					sound_muted = (sound_muted) ? false : true;//toggle sound
				}
			}

		}

		return true;
	}

	//control code//////////////////////////////////////////////////////////////////////////////////////////////
	public void back() {
		if (state == play) {
			music.stop();
			state = menu;
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		} else if (state == settings) {
			state = menu;
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		} else if (state == hiscores) {
			state = menu;
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		} else if (state == menu) {
			System.exit(0);
			activity.finish();
		} else if (state == gameover) {
			show_enter_highscore();
			if (sound_beep != 0 && !sound_muted)
				sp.play(sound_beep, 1, 1, 0, 0, 0);
		}
	}

	public void loadscore() {
		// load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		for (int i = 0; i < 10; i++) {
			hiscore[i] = hiscores.getInt("score" + i, 0);
			hiscorename[i] = hiscores.getString("name" + i, "---");
		}

	}

	public void savescore() {
		//load preferences
		SharedPreferences hiscores = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		SharedPreferences.Editor hiscores_editor = hiscores.edit();
		for (int i = 0; i < 10; i++) {
			hiscores_editor.putInt("score" + i, hiscore[i]);
			hiscores_editor.putString("name" + i, hiscorename[i]);
		}
		hiscores_editor.commit();

	}

	public void show_enter_highscore() {

		loadscore();
		if (score > hiscore[9]) {
			try {
				Class<?> classtostart = Class.forName(context.getPackageName() + ".EnterHiscore");
				Intent intent = new Intent(context, classtostart).putExtra("score", score);
				context.startActivity(intent);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		state = menu;
		score = 0;
	}

	public void create_obstacle(int type) {
		//add new obstacle at spesific x and y
		obstaclex.add(width + 100);
		if (groundobstacle[type])
			obstacley.add(height - (obstacle_img[type].getHeight() / 2));
		else
			obstacley.add(new Random().nextInt(height - (obstacle_img[type].getHeight())));
		obstacletype.add(type);
	}

	public boolean intersect(int x, int y, int width, int height, int pointx, int pointy) {

		if ((pointx > x) && (pointy > y) && (pointx < (x + width)) && (pointy < (y + height))) {
			return (true);
		}

		return false;
	}

	public boolean checkcollision(int box1x, int box1y, int width1, int height1, int box2x, int box2y, int width2, int height2) {
		Rect a = new Rect(box1x, box1y, box1x + width1, box1y + height1);
		Rect a2 = new Rect(box2x, box2y, box2x + width2, box2y + height2);
		if (a.intersect(a2)) {
			//no clollision
			return (true);
		} else {
			// collision
			return (false);
		}
	}

	public void obstacles_at_timeperiod(int starttime, int endtime, int[] obstacletypes, int generation_frequency) {
		if (((time >= starttime) && (endtime == -1)) || ((time >= starttime) && (time < endtime))) {
			if (time % (100 - generation_frequency) == 0) {
				int type = (int) (Math.random() * obstacletypes.length) + 1;
				create_obstacle(obstacletypes[type - 1]);
			}
		}
	}

	public void handle_obstacles() {
		//obstacle creator.-----------------------------------------------------CHANGE ME 3 ( add obstacle generations. Change their timeline..)
		obstacles_at_timeperiod(0, 500, new int[] { balloon1, tower1 }, 50);//generate balloon1 and tower 1 from the first 0 to 500 game time. Generation rate is 50.
		obstacles_at_timeperiod(500, 1000, new int[] { balloon1, balloon2, tower1 }, 80);
		obstacles_at_timeperiod(1000, 3000, new int[] { balloon1, balloon2, tower1, bomb }, 80);
		obstacles_at_timeperiod(3000, 7000, new int[] { balloon1, balloon2, tower1, tower2, bomb }, 80);
		obstacles_at_timeperiod(7000, 9000, new int[] { balloon1, balloon2, tower1, tower2, bomb, bomb }, 80);
		obstacles_at_timeperiod(9000, 12000, new int[] { balloon1, balloon2, balloon3, tower1, tower2, bomb, bomb }, 80);
		obstacles_at_timeperiod(12000, -1, new int[] { balloon1, balloon2, balloon3, tower1, tower2, bomb, bomb }, 90);//-1 here means  that it keeps generating these from 12000 to forever
		//feel free to copy paste 'obstacles_at_timeperiod()' to generate obstacles on a timeframe of your own
	}

	//initialise images
	public void initialise() {

		//initialise animations
		//plane initialise-----------------------------------------------------CHANGE ME 4 (plane sprite sheet change or anamation speed...)
		myplane = new gif(getResources(), R.drawable.myplane, 3, 1, 2, 2, 1, width * 0.15f);

		//scale images for best risponsiveness---------------------------------------------------------CHANGE ME 5 (from here you can change the scalling of each image)
		for (int i = 0; i < obstacle_img.length; i++) {
			obstacle_img[i] = Bitmap.createScaledBitmap(obstacle_img[i], (int) ((float) (width * 0.08f)), (int) (((float) (width * 0.08f) / obstacle_img[i].getWidth()) * obstacle_img[i].getHeight()), true);//the '0.08f' indicates the percentage of the screen taken by the image
		}

		play_img = Bitmap.createScaledBitmap(play_img, (int) ((float) (width * 0.2f)), (int) (((float) (width * 0.2f) / play_img.getWidth()) * play_img.getHeight()), true);
		settings_img = Bitmap.createScaledBitmap(settings_img, (int) ((float) (width * 0.14f)), (int) (((float) (width * 0.14f) / settings_img.getWidth()) * settings_img.getHeight()), true);
		highscores_img = Bitmap.createScaledBitmap(highscores_img, (int) ((float) (width * 0.16f)), (int) (((float) (width * 0.16f) / highscores_img.getWidth()) * highscores_img.getHeight()), true);
		exit_img = Bitmap.createScaledBitmap(exit_img, (int) ((float) (width * 0.08f)), (int) (((float) (width * 0.08f) / exit_img.getWidth()) * exit_img.getHeight()), true);
		home_img = Bitmap.createScaledBitmap(home_img, (int) ((float) (width * 0.08f)), (int) (((float) (width * 0.08f) / home_img.getWidth()) * home_img.getHeight()), true);

		selected_play_img = Bitmap.createScaledBitmap(play_img, (int) ((float) (width * 0.25f)), (int) (((float) (width * 0.25f) / play_img.getWidth()) * play_img.getHeight()), true);
		selected_settings_img = Bitmap.createScaledBitmap(settings_img, (int) ((float) (width * 0.2f)), (int) (((float) (width * 0.2f) / settings_img.getWidth()) * settings_img.getHeight()), true);
		selected_highscores_img = Bitmap.createScaledBitmap(highscores_img, (int) ((float) (width * 0.2f)), (int) (((float) (width * 0.2f) / highscores_img.getWidth()) * highscores_img.getHeight()), true);
		selected_exit_img = Bitmap.createScaledBitmap(exit_img, (int) ((float) (width * 0.10f)), (int) (((float) (width * 0.10f) / exit_img.getWidth()) * exit_img.getHeight()), true);
		selected_home_img = Bitmap.createScaledBitmap(home_img, (int) ((float) (width * 0.10f)), (int) (((float) (width * 0.10f) / home_img.getWidth()) * home_img.getHeight()), true);

		speed_img = Bitmap.createScaledBitmap(speed_img, (int) ((float) (width * 0.08f)), (int) (((float) (width * 0.08f) / speed_img.getWidth()) * speed_img.getHeight()), true);
		selected_speed_img = Bitmap.createScaledBitmap(speed_img, (int) ((float) (width * 0.12f)), (int) (((float) (width * 0.12f) / speed_img.getWidth()) * speed_img.getHeight()), true);
		pause_img = Bitmap.createScaledBitmap(pause_img, (int) ((float) (width * 0.04f)), (int) (((float) (width * 0.04f) / pause_img.getWidth()) * pause_img.getHeight()), true);
		pause_play_img = Bitmap.createScaledBitmap(pause_play_img, (int) ((float) (width * 0.04f)), (int) (((float) (width * 0.04f) / pause_play_img.getWidth()) * pause_play_img.getHeight()), true);
		cloud_img = Bitmap.createScaledBitmap(cloud_img, (int) ((float) (width * 0.07f)), (int) (((float) (width * 0.07f) / cloud_img.getWidth()) * cloud_img.getHeight()), true);
		smoke_img = Bitmap.createScaledBitmap(cloud_img, (int) ((float) (width * 0.02f)), (int) (((float) (width * 0.02f) / cloud_img.getWidth()) * cloud_img.getHeight()), true);
		background_img = Bitmap.createScaledBitmap(background_img, (int) ((float) (width * 1.02f)), (int) (((float) (width * 1.02f) / background_img.getWidth()) * background_img.getHeight()), true);
		boost_instruction_img = Bitmap.createScaledBitmap(boost_instruction_img, (int) ((float) (width * 0.15f)), (int) (((float) (width * 0.15f) / boost_instruction_img.getWidth()) * boost_instruction_img.getHeight()), true);
		instruction_img = Bitmap.createScaledBitmap(instruction_img, (int) ((float) (width * 0.15f)), (int) (((float) (width * 0.15f) / instruction_img.getWidth()) * instruction_img.getHeight()), true);
		sound_img = Bitmap.createScaledBitmap(sound_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / sound_img.getWidth()) * sound_img.getHeight()), true);
		sound_mute_img = Bitmap.createScaledBitmap(sound_mute_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / sound_mute_img.getWidth()) * sound_mute_img.getHeight()), true);
		music_img = Bitmap.createScaledBitmap(music_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / music_img.getWidth()) * music_img.getHeight()), true);
		music_mute_img = Bitmap.createScaledBitmap(music_mute_img, (int) ((float) (width * 0.06f)), (int) (((float) (width * 0.06f) / music_mute_img.getWidth()) * music_mute_img.getHeight()), true);

		//clouds-----------------------------------------------------------------------CHANGE ME 6 (change cloud initial x,y)
		cloud1 = new Particles(cloud_img, width, 2, 1, -1, (int) (width * 0.1f), (int) (width * 0.05f), 50);
		cloud1.y = (int) (Math.random() * width * 0.4f) + 40;
		cloud1.x = 100;

		cloud2 = new Particles(cloud_img, width, 2, 1, -1, (int) (width * 0.1f), (int) (width * 0.05f), 50);
		cloud2.y = (int) (Math.random() * width * 0.4f) + 40;
		cloud2.x = 500;

		//smoke
		smoke = new Particles(smoke_img, width, 1, 20, 270, 1, 1, 10);
		smoke.y = planey;
		smoke.x = planex + myplane.width / 4;

		//text and paint-----------------------------------------------------------------------CHANGE ME 7 (change fonts, text styles and colors)
		//fonts
		Typeface tragic = Typeface.createFromAsset(context.getAssets(), "tragic.ttf");

		//Text paints
		scoretext.setARGB(255, 222, 222, 222);
		scoretext.setTypeface(tragic);
		scoretext.setTextSize(fontpercent_screenheight(8));
		scoretext.setAntiAlias(true);

		stroke_scoretext.setARGB(255, 40, 40, 40);
		stroke_scoretext.setTypeface(tragic);
		stroke_scoretext.setTextSize(fontpercent_screenheight(8));
		stroke_scoretext.setAntiAlias(true);
		stroke_scoretext.setStyle(Style.STROKE);
		stroke_scoretext.setStrokeWidth(4);

		//title paint
		titletext.setTypeface(tragic);
		titletext.setARGB(255, 38, 134, 181);
		titletext.setTextSize(fontpercent_screenheight(11));
		titletext.setAntiAlias(true);

		stroke_titletext.setTypeface(tragic);
		stroke_titletext.setTextSize(fontpercent_screenheight(11));
		stroke_titletext.setARGB(255, 0, 0, 0);
		stroke_titletext.setStyle(Style.STROKE);
		stroke_titletext.setStrokeWidth(4);
		stroke_titletext.setAntiAlias(true);

		//menu text
		menutext.setTypeface(tragic);
		menutext.setARGB(255, 40, 40, 40);
		menutext.setTextSize(fontpercent_screenheight(5));
		menutext.setAntiAlias(true);

		//pause text
		gameplaytext.setTypeface(tragic);
		gameplaytext.setAntiAlias(true);
		gameplaytext.setTextSize(fontpercent_screenheight(10));
		gameplaytext.setARGB(255, 222, 222, 222);

		//coloring Paints
		semitrasparentwhite.setARGB(80, 222, 222, 222);
		trasparentblack.setARGB(150, 0, 0, 0);
		yellow_light.setARGB(150, 255, 218, 94);
		blue.setARGB(255, 38, 134, 181);
		black.setARGB(255, 40, 40, 40);
		white.setARGB(255, 222, 222, 222);

		backgroundx2 = width;

		initialised = true;

	}

	//program loop///////////////////////////////////////////////////////////////////////////////////////////////
	public void run() {
		while (isRunning) {
			if (ourholder.getSurface().isValid()) {
				now = SystemClock.uptimeMillis();
				if (now - laststep > 25) {
					//int timelag = (int) (now - laststep) - 25;
					//	System.out.println("lag " + timelag);

					Step();
					Canvas canvas = ourholder.lockCanvas();
					Draw(canvas);
					ourholder.unlockCanvasAndPost(canvas);

					laststep = SystemClock.uptimeMillis();
				}

			}

		}
	}

	public void Step() {

		if (state == play) {
			if (instructions) {

			} else {
				if (!paused) {
					if (!levelstarted) {

						now = SystemClock.uptimeMillis();
						countdown = 3 - (int) ((now - start) / 1000);
						if (countdown == 0) {
							levelstarted = true;
						}

					} else {
						//initialise
						if (width != -1 && height != -1) {

							//move background----------------------------------------CHANGE ME 8 (background speed)
							backgroundx1 -= 5.5f + ((onspeed) ? 1 : 0);
							backgroundx2 -= 5.5f + ((onspeed) ? 1 : 0);
							if (backgroundx1 < -width)
								backgroundx1 = width;
							if (backgroundx2 < -width)
								backgroundx2 = width;

							//move clouds
							cloud1.step(cloud1.x - 2 - ((onspeed) ? 1 : 0), cloud1.y);
							if (cloud1.x < -100)
								cloud1.step(width + 100, (int) (Math.random() * width * 0.4f) + 40);

							cloud2.step(cloud2.x - 2 - ((onspeed) ? 1 : 0), cloud2.y);
							if (cloud2.x < -100)
								cloud2.step(width + 100, (int) (Math.random() * width * 0.4f) + 40);

							//move smoke
							smoke.step(planex + myplane.width / 8, planey - +myplane.height / 8);
							smoke.speed = (int) ((onspeed) ? 40 : 20);
							smoke.particle_life = (int) ((onspeed) ? 8 : 10);
							smoke.particle_direction = 270 + (int) (sensory * 5);

							//move plane
							if (speedy == 0)
								speedy = 0.5f;
							//update plane speed-------------------------------------------------CHANGE ME 9(change the '0.0009f' to change plane acceleration)
							speedy = speedy + ((sensory - calibratey) * 0.0009f * width * ((onspeed) ? 1.001f : 1));

							//plane maximum speed
							if (speedy > width * 0.01f)
								speedy = width * 0.01f;
							if (speedy < -width * 0.01f)
								speedy = -width * 0.01f;

							//plane stable position
							if ((Math.abs((sensory - calibratey)) < width * 0.0001f) && Math.abs(speedy) < width * 0.008f)
								speedy = 0;

							planey = planey + speedy;

							//out of bounds protection
							if (planey > height - myplane.height) {
								speedy = -Math.abs(speedy / 20);
								planey = height - myplane.height;
							}
							if (planey < 0) {
								speedy = Math.abs(speedy / 20);
								planey = 0;
							}

							myplane.rotate(sensory * 5);
							myplane.speed  = (int) ((onspeed) ? 10 : 2);

							//boost
							if (planex < width - myplane.width) {
								if (planex > 50)
									planex += ((onspeed) ? 1.002f : -1.002f);
								else
									planex += ((onspeed) ? 1.002f : 0);
							}

							//obstacles creation
							time++;
							handle_obstacles();

							//handle all obstacles
							for (int i = 0; i <= obstaclex.size() - 1; i++) {

								//move obstacles
								obstaclex.set(i, (int) (obstaclex.get(i) - (obstacle_speed[obstacletype.get(i)] * width * 0.003f) - ((onspeed) ? 6 : 0)));

								//delete if out of screen
								if (obstaclex.get(i) < -100) {
									obstaclex.remove(i);
									obstacley.remove(i);
									obstacletype.remove(i);
								} else {

									//collision detection
									if (checkcollision(obstaclex.get(i) - (obstacle_img[obstacletype.get(i)].getWidth() / 2), obstacley.get(i) - (obstacle_img[obstacletype.get(i)].getHeight() / 2), obstacle_img[obstacletype.get(i)].getWidth(), obstacle_img[obstacletype.get(i)].getHeight(), (int) planex, (int) planey - (myplane.height / 4), (int) (myplane.width / 1.3f), myplane.height / 2)) {
										if (obstacle_points[obstacletype.get(i)] == -1) {
											state = gameover;
											music.stop();
										} else {
											score += obstacle_points[obstacletype.get(i)];
											if (sound_score != 0 && !sound_muted)
												sp.play(sound_score, 1, 1, 0, 0, 0);
										}
										obstaclex.remove(i);
										obstacley.remove(i);
										obstacletype.remove(i);
									}

								}

							}

						}

					}
				}
			}
		}
	}

	public void Draw(Canvas canvas) {
		if (!initialised) {
			width = canvas.getWidth();
			height = canvas.getHeight();
			initialise();
		} else {

			//set background----------------------------------------------------------CHANGE ME 10 (change background gradient color from here)
			background.setShader(new LinearGradient(width / 2, 0, width / 2, height, Color.argb(255, 29, 129, 250), Color.argb(255, 70, 151, 250), Shader.TileMode.CLAMP));
			menu_background.setShader(new LinearGradient(width / 2, 0, width / 2, height, Color.argb(255, 255, 190, 28), Color.argb(255, 255, 200, 30), Shader.TileMode.CLAMP));

			//if (onexit)
			//canvas.drawBitmap(selected_exit_img, (width * 0.9f) - (selected_exit_img.getWidth() / 2), (height * 0.9f) - (selected_exit_img.getHeight() / 2), null);
			//else

			if (state == menu) {
				//draw background
				canvas.drawRect(0, 0, width, height, menu_background);
				canvas.drawCircle((width / 2.4f), (height / 1.1f), (height * 0.3f), yellow_light);
				canvas.drawCircle((width * 0.90f), (height * 0.4f), (height * 0.15f), yellow_light);
				canvas.drawCircle((width / 4f), (height / 7), (height * 0.4f), yellow_light);

				//draw title
				canvas.drawText(context.getString(R.string.app_name), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.app_name)) / 2), (height / 7), stroke_titletext);
				canvas.drawText(context.getString(R.string.app_name), (width / 2) - (titletext.measureText(context.getString(R.string.app_name)) / 2), (height / 7), titletext);

				//sound control
				if (music_muted)
					canvas.drawBitmap(music_mute_img, (width * 0.95f) - (music_mute_img.getWidth() / 2), music_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(music_img, (width * 0.95f) - (music_img.getWidth() / 2), music_img.getHeight() * 0.12f, null);

				if (sound_muted)
					canvas.drawBitmap(sound_mute_img, (width * 0.88f) - (sound_mute_img.getWidth() / 2), sound_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(sound_img, (width * 0.88f) - (sound_img.getWidth() / 2), sound_img.getHeight() * 0.12f, null);

				//draw menu buttons
				if (onplay)
					canvas.drawBitmap(selected_play_img, (width / 2.4f) - (selected_play_img.getWidth() / 2), (height / 1.5f) - (selected_play_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(play_img, (width / 2.4f) - (play_img.getWidth() / 2), (height / 1.5f) - (play_img.getHeight() / 2), null);

				if (onhighscores)
					canvas.drawBitmap(selected_highscores_img, (width * 0.70f) - (selected_highscores_img.getWidth() / 2), (height * 0.4f) - (selected_highscores_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(highscores_img, (width * 0.70f) - (highscores_img.getWidth() / 2), (height * 0.4f) - (highscores_img.getHeight() / 2), null);

				if (onsettings)
					canvas.drawBitmap(selected_settings_img, (width / 4f) - (selected_settings_img.getWidth() / 2), (height / 2.2f) - (selected_settings_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(settings_img, (width / 4f) - (settings_img.getWidth() / 2), (height / 2.2f) - (settings_img.getHeight() / 2), null);

				if (onexit)
					canvas.drawBitmap(selected_exit_img, (width * 0.9f) - (selected_exit_img.getWidth() / 2), (height * 0.9f) - (selected_exit_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(exit_img, (width * 0.9f) - (exit_img.getWidth() / 2), (height * 0.9f) - (exit_img.getHeight() / 2), null);

			}

			if (state == settings) {
				//draw background
				canvas.drawRect(0, 0, width, height, menu_background);
				canvas.drawCircle((width / 2.4f), (height / 1.1f), (height * 0.3f), yellow_light);
				canvas.drawCircle((width * 0.90f), (height * 0.4f), (height * 0.15f), yellow_light);
				canvas.drawCircle((width / 4f), (height / 7), (height * 0.4f), yellow_light);

				//draw title
				canvas.drawText(context.getString(R.string.settings_title), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.settings_title)) / 2), (height / 7), stroke_titletext);
				canvas.drawText(context.getString(R.string.settings_title), (width / 2) - (titletext.measureText(context.getString(R.string.settings_title)) / 2), (height / 7), titletext);

				//calibrate tool
				canvas.drawRect((width / 2) - (width / 4), (height / 2) - (width / 20), (width / 2) + (width / 4), (height / 2) + (width / 20), black);
				if (oncalibrate)
					canvas.drawCircle((width / 2) - ((sensory - calibratey) * 15), (height / 2), (width / 30), yellow_light);
				else
					canvas.drawCircle((width / 2) - ((sensory - calibratey) * 15), (height / 2), (width / 30), white);
				canvas.drawText(context.getString(R.string.calibrate_instructions), (width / 2) - (menutext.measureText(context.getString(R.string.calibrate_instructions)) / 2), (height * 0.65f), menutext);

				//back to menu button
				if (onmenu)
					canvas.drawBitmap(selected_home_img, (width * 0.9f) - (selected_home_img.getWidth() / 2), (height * 0.9f) - (selected_home_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(home_img, (width * 0.9f) - (home_img.getWidth() / 2), (height * 0.9f) - (home_img.getHeight() / 2), null);
			}

			if (state == gameover) {
				//draw background
				canvas.drawRect(0, 0, width, height, menu_background);
				canvas.drawCircle((width / 2.4f), (height / 1.1f), (height * 0.3f), yellow_light);
				canvas.drawCircle((width * 0.90f), (height * 0.4f), (height * 0.15f), yellow_light);
				canvas.drawCircle((width / 4f), (height / 7), (height * 0.4f), yellow_light);

				//draw title
				canvas.drawText(context.getString(R.string.game_over), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.game_over)) / 2), (height / 2) - titletext.getTextSize(), stroke_titletext);
				canvas.drawText(context.getString(R.string.game_over), (width / 2) - (titletext.measureText(context.getString(R.string.game_over)) / 2), (height / 2) - titletext.getTextSize(), titletext);

				//click to continue text
				canvas.drawText(context.getString(R.string.game_over_continue), (width / 2) - (menutext.measureText(context.getString(R.string.game_over_continue)) / 2), (height / 2) + titletext.getTextSize(), menutext);

			}

			if (state == hiscores) {

				//draw background
				canvas.drawRect(0, 0, width, height, menu_background);
				canvas.drawCircle((width / 2.4f), (height / 1.1f), (height * 0.3f), yellow_light);
				canvas.drawCircle((width * 0.90f), (height * 0.4f), (height * 0.15f), yellow_light);
				canvas.drawCircle((width / 4f), (height / 7), (height * 0.4f), yellow_light);

				//draw title
				canvas.drawText(context.getString(R.string.highscore_title), (width / 2) - (stroke_titletext.measureText(context.getString(R.string.highscore_title)) / 2), (height / 7), stroke_titletext);
				canvas.drawText(context.getString(R.string.highscore_title), (width / 2) - (titletext.measureText(context.getString(R.string.highscore_title)) / 2), (height / 7), titletext);

				//back to menu button
				if (onmenu)
					canvas.drawBitmap(selected_home_img, (width * 0.9f) - (selected_home_img.getWidth() / 2), (height * 0.9f) - (selected_home_img.getHeight() / 2), null);
				else
					canvas.drawBitmap(home_img, (width * 0.9f) - (home_img.getWidth() / 2), (height * 0.9f) - (home_img.getHeight() / 2), null);

				//hiscores
				for (int i = 0; i < 10; i++) {
					canvas.drawText(hiscorename[i], (width / 2) - (width / 4), (height / 2) - (height / 3.5f) + (i * menutext.getTextSize() * 1.5f), menutext);
					canvas.drawText("" + hiscore[i], (width / 2) + (width / 6), (height / 2) - (height / 3.5f) + (i * menutext.getTextSize() * 1.5f), menutext);
				}

			}

			if (state == play) {
				//draw sky background
				canvas.drawRect(0, 0, width, height, background);

				//draw cloud
				cloud1.draw(canvas);
				cloud2.draw(canvas);
				smoke.draw(canvas);

				//draw background
				canvas.drawBitmap(background_img, backgroundx1, height - background_img.getHeight(), null);
				canvas.drawBitmap(background_img, backgroundx2, height - background_img.getHeight(), null);

				//draw obstacles
				for (int i = 0; i <= obstaclex.size() - 1; i++) {
					canvas.drawBitmap(obstacle_img[obstacletype.get(i)], obstaclex.get(i) - (obstacle_img[obstacletype.get(i)].getWidth() / 2), obstacley.get(i) - (obstacle_img[obstacletype.get(i)].getHeight() / 2), null);
				}

				//draw plane
				myplane.draw(canvas, (int) planex, (int) planey - ((myplane.height) / 2), 0);

				//draw score
				menutext.setARGB(255, 222, 222, 222);
				canvas.drawText("Score: " + score, width * 0.03f, height * 0.09f, stroke_scoretext);
				canvas.drawText("Score: " + score, width * 0.03f, height * 0.09f, scoretext);
				menutext.setARGB(255, 40, 40, 40);
				//pause
				if (paused)
					canvas.drawBitmap(pause_play_img, (width / 2) - (pause_play_img.getWidth() / 2), pause_play_img.getHeight() * 0.2f, null);
				else
					canvas.drawBitmap(pause_img, (width / 2) - (pause_img.getWidth() / 2), pause_img.getHeight() * 0.2f, null);

				//speed
				if (onspeed)
					canvas.drawBitmap(selected_speed_img, width * 0.92f - (selected_speed_img.getWidth()/2) , height* 0.88f - (selected_speed_img.getHeight()/2), null);
				else
					canvas.drawBitmap(speed_img, width * 0.92f - (speed_img.getWidth()/2) , height* 0.88f - (speed_img.getHeight()/2), null);

				//sound control
				if (music_muted)
					canvas.drawBitmap(music_mute_img, (width * 0.95f) - (music_mute_img.getWidth() / 2), music_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(music_img, (width * 0.95f) - (music_img.getWidth() / 2), music_img.getHeight() * 0.12f, null);

				if (sound_muted)
					canvas.drawBitmap(sound_mute_img, (width * 0.88f) - (sound_mute_img.getWidth() / 2), sound_mute_img.getHeight() * 0.12f, null);
				else
					canvas.drawBitmap(sound_img, (width * 0.88f) - (sound_img.getWidth() / 2), sound_img.getHeight() * 0.12f, null);

				if (instructions) {
					canvas.drawRect((width / 2) - (width / 4), (height / 2) - (height / 4), (width / 2) + (width / 4), (height / 2) + (height / 4), semitrasparentwhite);

					menutext.setTypeface(Typeface.DEFAULT);
					StaticLayout instructionlayout = new StaticLayout(context.getString(R.string.instructions), new TextPaint(menutext), (int) ((width / 4.4f) + (width / 4)), Layout.Alignment.ALIGN_NORMAL, 1.3f, 0, false);
					canvas.translate((width / 2) - (width / 4.4f), (height / 2) + (height / 30)); //position the text
					instructionlayout.draw(canvas);
					canvas.translate(-((width / 2) - (width / 4.4f)), -((height / 2) + (height / 30))); //position the text

					menutext.setARGB(255, 222, 222, 222);
					canvas.drawText(context.getString(R.string.instructions_continue), (width / 2) - (menutext.measureText(context.getString(R.string.instructions_continue)) / 2), (height / 2) + (height / 3), menutext);
					menutext.setARGB(255, 40, 40, 40);
					
					//instruction image
					canvas.drawBitmap(instruction_img, (width/2) - (instruction_img.getWidth()/2) , (height*0.4f) - (instruction_img.getHeight()/2), null);

					canvas.drawBitmap(boost_instruction_img, width * 0.83f - (boost_instruction_img.getWidth()/2) , height* 0.73f - (boost_instruction_img.getHeight()/2), null);


				} else {
					if (!levelstarted) {
						canvas.drawText(context.getString(R.string.Prepare), (width / 2) - (gameplaytext.measureText(context.getString(R.string.Prepare)) / 2), (height / 2) - (gameplaytext.getTextSize() / 2), gameplaytext);
						canvas.drawText("" + countdown, (width / 2) - (gameplaytext.measureText("" + countdown) / 2), (height / 2) + (gameplaytext.getTextSize()), gameplaytext);

					}
				}

				if (paused) {
					canvas.drawRect(0, 0, width, height, trasparentblack);
					canvas.drawText(context.getString(R.string.Pause), (width / 2) - (gameplaytext.measureText(context.getString(R.string.Pause)) / 2), (height / 2) - (gameplaytext.getTextSize() / 2), gameplaytext);
				}

			}

		}
	}

	public int fontpercent_screenheight(double d) {
		//get resolution
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

		int px = (int) ((float) dm.heightPixels * ((float) d / 100));
		float dp = px / dm.density;
		return (int) dp;
	}

	//enviorment handlars//////////////////////////////////////////////////////////////////////////////////////////
	public void pause() {
		isRunning = false;
		if (state == play)
			music.stop();
		if (state == play && levelstarted && !instructions)
			paused = true;

		while (true) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
		thread = null;
		sm.unregisterListener(this);
	}

	public void resume() {
		isRunning = true;
		thread = new Thread(this);
		thread.start();
		sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
	}

}
