package com.exercicisgokyle.uf2pr1_simon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private var soundRed = 0
    private var soundGreen = 0
    private var soundBlue = 0

    private var isGameStarted = false

    //Lista para almacenar la secuencia de colores del juego
    private val sequence = mutableListOf<Int>()
    //Lista para almacenar la secuencia de colores del usuario
    private val userSequence = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Crear Vista del juego
        val gameView = GameView(this)
        setContentView(gameView)

        // Configurar atributos de audio para el SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Inicializar SoundPool
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        // Cargar sonidos
        soundRed = soundPool.load(this, R.raw.red_sound, 1)
        soundGreen = soundPool.load(this, R.raw.green_sound, 1)
        soundBlue = soundPool.load(this, R.raw.blue_sound, 1)

        // Agregar una secuencia de colores predefinida
        sequence.addAll(listOf(1, 0, 2, 0, 1))

    }

    inner class GameView(context: Context) : View(context) {

        private var highlightedColor: Int? = null

        // Definir el tamaño de las cajas de colores
        private val boxSize = 300f
        // Espacio entre cajas
        private val boxSpacing = 50f

        // Configuración de pinturas para el Simon
        private val paintRed = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        private val paintGreen = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.FILL
        }
        private val paintBlue = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        private val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 120f
            isFakeBoldText = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private val paintText2 = Paint().apply {
            color = Color.WHITE
            textSize = 120f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        private  val paintBlack = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        //
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas?.apply {
                // Establecer el color de fondo del lienzo, el blanco me hace daño a la vista
                drawColor(Color.LTGRAY)

                // Calcular el centro horizontal
                val totalWidth = 3 * boxSize + 2 * boxSpacing
                val startX = (width - totalWidth) / 2
                val startY = (height - boxSize) / 2  // Adjust this value as needed

                // Dibujar el botón de inicio
                val startYButton = startY + boxSize + boxSpacing
                drawRect(startX, startYButton, startX + totalWidth, startYButton + boxSize, paintBlack)
                drawText("Start", width / 2f, startYButton + boxSize / 2 + paintText.textSize / 2, paintText2)

                // // Dibujar el Título Simon Says
                drawText("Simon Says", width / 2f, startY - 100f, paintText)

                // Dibujar las cajas en el centro de la pantalla
                drawRect(startX, startY, startX + boxSize, startY + boxSize, getPaintColor(paintRed, 0))
                drawRect(startX + boxSize + boxSpacing, startY, startX + 2 * boxSize + boxSpacing, startY + boxSize, getPaintColor(paintGreen, 1))
                drawRect(startX + 2 * (boxSize + boxSpacing), startY, startX + 3 * boxSize + 2 * boxSpacing, startY + boxSize, getPaintColor(paintBlue, 2))
            }
        }

        // función para cambiar el color de las cajas, highlighted y devolverlas a su color anterior
        private fun getPaintColor(paint: Paint, colorIndex: Int): Paint {
            return if (highlightedColor == colorIndex) {
                paint.apply {
                    color = when (colorIndex) {
                        0 -> Color.rgb(255, 102, 102) // Rojo claro
                        1 -> Color.rgb(102, 255, 102) // Verde claro
                        2 -> Color.rgb(102, 178, 255) // Azul claro
                        else -> Color.BLACK // predeterminado
                    }
                }
            } else {
                paint.apply {
                    color = when (colorIndex) {
                        0 -> Color.RED // Rojo normal
                        1 -> Color.GREEN // Verde normal
                        2 -> Color.BLUE // Azul normal
                        else -> Color.BLACK // predeterminado
                    }
                }
            }
        }

        fun highlightColor(index: Int) {
            // Introduce un pequeño retraso antes de reproducir el sonido
            val handler = android.os.Handler(Looper.getMainLooper())
            handler.postDelayed({
                // Reproduce el sonido correspondiente al color
                when (index) {
                    0 -> soundPool.play(soundRed, 1.0f, 1.0f, 0, 0, 1.0f)
                    1 -> soundPool.play(soundGreen, 1.0f, 1.0f, 0, 0, 1.0f)
                    2 -> soundPool.play(soundBlue, 1.0f, 1.0f, 0, 0, 1.0f)
                }
            }, 30) // Retraso de 30 milisegundos

            // Resalta el color
            highlightedColor = index
            invalidate() // Redibuja la vista con el color resaltado

            // Configura otro manejador para desresaltar el color después de 30ms
            handler.postDelayed({
                highlightedColor = null
                invalidate() // Redibuja la vista con el color original
            }, 60) // Desresalta después de 30ms adicionales
        }

        // función para empezar el juego
        fun startGame() {
            // limpiar el array de la secuencia de botones del jugador
            userSequence.clear()
            isGameStarted = true;
            // Corrutina para realizar la secuencia de colores para el usuario (los valores de la secuencia se han inicializado en el onCreate)
            CoroutineScope(Dispatchers.Main).launch {
                // por cada caja de la secuencia emite el sonido y lo cambia de color para avisar al usuario
                sequence.forEach { colorIndex ->
                    highlightColor(colorIndex)
                    delay(1000) // Wait for 1 second before highlighting the next color
                }
            }
        }

        // función para poder interactuar con los elementos de la pantalla
        override fun onTouchEvent(event: MotionEvent?): Boolean {
            event?.let {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // obtener las coordenadas para la acción del evento
                        val x = event.x
                        val y = event.y

                        // Calcula la posición de las cajas
                        val totalWidth = 3 * boxSize + 2 * boxSpacing
                        val startX = (width - totalWidth) / 2
                        val startY = (height - boxSize) / 2

                        when {
                            // Comprueba si el tap se ha realizado dentro de la caja y si el juego ha empezado, esto es para que no se pueda interactuar
                            // con las cajas
                            x in startX..startX + boxSize && y in startY..startY + boxSize && isGameStarted -> {
                                // realiza la función highlightColor que cambia el color del objeto
                                highlightColor(0)
                                // añade a la secuencia del usuario el color
                                    userSequence.add(0)
                            }
                            x in startX + boxSize + boxSpacing..startX + 2 * boxSize + boxSpacing && y in startY..startY + boxSize && isGameStarted -> {
                                highlightColor(1)
                                    userSequence.add(1)
                            }
                            // Check if the touch is within the bounds of the blue box
                            x in startX + 2 * (boxSize + boxSpacing)..startX + 3 * boxSize + 2 * boxSpacing && y in startY..startY + boxSize && isGameStarted -> {
                                highlightColor(2)
                                    userSequence.add(2)
                            }
                            // caja para empezar la partida
                            x in startX..startX + totalWidth && y in startY + boxSize + boxSpacing..startY + 2 * boxSize + boxSpacing -> {
                                startGame()
                            }

                            else -> {}

                            }
                        // Si el juego ha comenzado y la secuencia del usuario no está vacía, realiza lo siguiente:
                        if (isGameStarted && userSequence.isNotEmpty()) {
                            // Verifica si la longitud de la secuencia del usuario es mayor que la del juego
                            // o si el último elemento de la secuencia del usuario no coincide con la secuencia del juego
                            if (userSequence.size > sequence.size || userSequence.last() != sequence[userSequence.size - 1]) {
                                // Mostrar un mensaje de que el usuario ha perdido
                                Toast.makeText(context, "Incorrecto, has perdido", Toast.LENGTH_SHORT).show()
                                // Limpiar la secuencia del usuario
                                userSequence.clear()
                                // Establecer isGameStarted como falso para reiniciar el juego
                                isGameStarted = false
                            } else if (userSequence.size == sequence.size) {
                                // Si la longitud de la secuencia del usuario es igual a la del juego
                                // Mostrar un mensaje que el usuario ha ganado
                                Toast.makeText(context, "¡Has ganado!", Toast.LENGTH_SHORT).show()
                                userSequence.clear()
                                // Establecer isGameStarted como falso para reiniciar el juego
                                isGameStarted = false
                            }
                        }
                    }

                    else -> {}
                }
            }
            return true
        }
    }
}