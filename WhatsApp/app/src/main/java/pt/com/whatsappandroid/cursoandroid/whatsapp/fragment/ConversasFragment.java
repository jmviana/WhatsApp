package pt.com.whatsappandroid.cursoandroid.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import pt.com.whatsappandroid.cursoandroid.whatsapp.R;
import pt.com.whatsappandroid.cursoandroid.whatsapp.activity.ConversaActivity;
import pt.com.whatsappandroid.cursoandroid.whatsapp.adapter.ConversaAdapter;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Base64Custom;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Conversa;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversasFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<Conversa> adapter;
    private ArrayList<Conversa> conversas;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListenerConversas;

    public ConversasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart(){
        super.onStart();
        firebaseRef.addValueEventListener( valueEventListenerConversas );
    }

    @Override
    public void onStop(){
        super.onStop();
        firebaseRef.removeEventListener( valueEventListenerConversas );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Instanciar objetos
        conversas = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_conversas, container, false );

        //Monta listView e adapter
        listView = view.findViewById( R.id.lv_conversas );
        //adapter = new ArrayAdapter( getActivity(), R.layout.lista_conversas, conversas );
        adapter = new ConversaAdapter( getActivity(), conversas );
        listView.setAdapter( adapter );

        //Recuperar conversas do firebase
        Preferencias preferencias = new Preferencias( getActivity() );
        String idUsuarioLogado = preferencias.getIdentificador();
        firebaseRef = ConfiguracaoFirebase.getFirebase().child("conversas").child( idUsuarioLogado );

        //Listener para recuperar conversas
        valueEventListenerConversas = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Limpar lista
                conversas.clear();

                //Listar conversas
                for (DataSnapshot dados : dataSnapshot.getChildren()){

                    Conversa conversa = dados.getValue(Conversa.class);
                    conversas.add(conversa);

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //Adicionar evento de clique na lista
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent( getActivity(), ConversaActivity.class );

                //recupera dados a serem passados
                Conversa conversa = conversas.get( position );

                //enviando dados para conversa activity
                intent.putExtra( "nome", conversa.getNome() );
                String email = Base64Custom.decodificarBase64( conversa.getIdUsuario() );
                intent.putExtra( "email", email );
                intent.putExtra( "imagemDest", conversa.getImageLocation() );

                startActivity( intent );
            }
        } );

        return view;
    }

}
