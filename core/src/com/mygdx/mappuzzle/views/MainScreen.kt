package com.mygdx.mappuzzle

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureAdapter
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

/**
 * Main Screen of the game, this is where the level will be shown and the game is actually played
 * @param game Main game object, contains useful objects that need to be passed to multiple screens
 */
class MainScreen(var game : MapPuzzle) : Screen, GestureAdapter() {
    //this object is used to load levels, probably a better way of doing this
    var l = LevelLoader(game)

    //this color determines the background
    var color : Color = Color.LIGHT_GRAY

    var level : Level = Level()
    var camera : OrthographicCamera = OrthographicCamera();

    /**
     * this function is called when the screen is shown, mostly used ot initialise values.
     */
    override fun show() {
        //makes it so mouse clicks are registered properly
        val gd : GestureDetector = GestureDetector(this)
        Gdx.input.inputProcessor = gd

        //creates and sets the level to random level in the list
        //game.levels!![Random.nextInt(game.levels!!.size)]
        level = l.createLevel(game.levels!![Random.nextInt(game.levels!!.size)])
        camera.viewportWidth=(level.outline!!.width);
        camera.viewportHeight= (level.outline!!.width)*(Gdx.graphics.height.toFloat()/Gdx.graphics.width.toFloat());
        camera.position.set(camera.viewportWidth/2,camera.viewportHeight/2,0f)
        level.outline!!.polygon!!.setPosition(level.outline!!.offsetX - level.outline!!.minX, camera.viewportHeight-(level.outline!!.height))

        for(p in level.pieces){
            p.polygon!!.setPosition(Random.nextDouble(0.0, (camera.viewportWidth).toDouble()).toFloat(),
                    Random.nextDouble(0.0, camera.viewportHeight/2.toDouble()).toFloat())
        }

    }



    val img = Texture(Gdx.files.internal("complete.png"));

    /**
     * Main render loop of the screen, is called repeatedly every few miliseconds.
     * @param delta the time between render calls in miliseconds
     */
    override fun render(delta: Float) {
        var completed = true;
        camera.update();
        game.batch!!.projectionMatrix = camera.combined;
        ScreenUtils.clear(color)
        game.batch!!.begin()
        level.draw(game.batch!!)

        for(p in level.pieces){
            if(!p.checkPos(level.outline!!.minX,level.outline!!.minY, camera.viewportHeight, level.outline!!.height, level.outline!!.width)){
                completed = false;
            }
        }
        if(completed){
            game.batch!!.draw(img, 0f, camera.viewportHeight/2, camera.viewportWidth, camera.viewportWidth*(1467f/2200f))
        }

        game.batch!!.end()

    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
    }


    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        currentPiece = null;
        dragging = false;
        return true
    }
    var dragging = false;
    var currentPiece : Piece? = null;

    override fun pan(x : Float,y :Float,deltaX : Float,deltaY : Float): Boolean {
        val worldCoordinates = camera.unproject(Vector3(x.toFloat(), y.toFloat(), 0f))
        if(!dragging) {

            currentPiece = level.get(worldCoordinates.x, worldCoordinates.y)
            if (currentPiece != null) {
                dragging = true
                currentPiece!!.setX(worldCoordinates.x - (currentPiece!!.width/2 + (currentPiece!!.minX - currentPiece!!.offsetX)))
                currentPiece!!.setY(worldCoordinates.y - (currentPiece!!.height/2 + (currentPiece!!.minY - currentPiece!!.offsetY)))
            }else{
                dragging = true
            }
        }else{
            if(currentPiece!=null) {
                currentPiece!!.setX(worldCoordinates.x - (currentPiece!!.width / 2 + (currentPiece!!.minX - currentPiece!!.offsetX)))
                currentPiece!!.setY(worldCoordinates.y - (currentPiece!!.height / 2 + (currentPiece!!.minY - currentPiece!!.offsetY)))
            }else{
                val deltaX= -deltaX
                if(deltaX < 0){
                    if(camera.position.x + (deltaX/camera.viewportWidth)*camera.zoom*0.5f > 0 - camera.viewportWidth*0.1f){
                        camera.position.x += (deltaX/camera.viewportWidth)*camera.zoom*0.5f
                    }
                }else{
                    if(camera.position.x + (deltaX/camera.viewportWidth)*camera.zoom < camera.viewportWidth*1.1f){
                        camera.position.x += (deltaX/camera.viewportWidth)*camera.zoom*0.5f
                    }
                }
                if(deltaY < 0){
                    if(camera.position.y + (deltaY/camera.viewportHeight)*camera.zoom*0.5f > 0- camera.viewportHeight*0.1f){
                        camera.position.y += (deltaY/camera.viewportHeight)*camera.zoom*0.5f
                    }
                }else{
                    if(camera.position.y +(deltaY/camera.viewportHeight)*camera.zoom*0.5f < camera.viewportHeight*1.1f){
                        camera.position.y += (deltaY/camera.viewportHeight)*camera.zoom*0.5f
                    }
                }
            }
        }

        return true
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        val dist : Float = (distance/initialDistance)
        if(dist>1) {
            if(camera.zoom < 1.2f) {
                camera.zoom += 0.05f
            }
        }else{
            if(camera.zoom > 0.1f) {
                camera.zoom -= 0.02f
            }
        }
        return true
    }


}