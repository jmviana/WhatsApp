package pt.com.whatsappandroid.cursoandroid.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import pt.com.whatsappandroid.cursoandroid.whatsapp.adapter.ContactoAdapter;
import pt.com.whatsappandroid.cursoandroid.whatsapp.config.ConfiguracaoFirebase;
import pt.com.whatsappandroid.cursoandroid.whatsapp.helper.Preferencias;
import pt.com.whatsappandroid.cursoandroid.whatsapp.model.Contacto;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactosFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<Contacto> adapter;
    private ArrayList<Contacto> contactos;
    private DatabaseReference firebaseRef;
    private ValueEventListener valueEventListenerContactos;

    public ContactosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart(){
        super.onStart();
        firebaseRef.addValueEventListener( valueEventListenerContactos );
    }

    @Override
    public void onStop(){
        super.onStop();
        firebaseRef.removeEventListener( valueEventListenerContactos );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Instanciar objetos
        contactos = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate( R.layout.fragment_contactos, container, false );

        //Monta listView e adapter
        listView = view.findViewById( R.id.lv_contactos );
        //adapter = new ArrayAdapter( getActivity(), R.layout.lista_contacto, contactos );
        adapter = new ContactoAdapter( getActivity(), contactos );
        listView.setAdapter( adapter );

        //Recuperar contactos do firebase
        Preferencias preferencias = new Preferencias( getActivity() );
        String idUsuarioLogado = preferencias.getIdentificador();
        firebaseRef = ConfiguracaoFirebase.getFirebase().child("contactos").child( idUsuarioLogado );

        //Listener para recuperar contactos
        valueEventListenerContactos = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Limpar lista
                contactos.clear();

                //Listar contactos
                for (DataSnapshot dados : dataSnapshot.getChildren()){

                    Contacto contacto = dados.getValue(Contacto.class);
                    contactos.add(contacto);

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent( getActivity(), ConversaActivity.class );

                //recupera dados a serem passados
                Contacto contacto = contactos.get( position );

                //enviando dados para conversa activity
                intent.putExtra( "nome", contacto.getNome() );
                intent.putExtra( "email", contacto.getEmail() );
                intent.putExtra( "imagemDest", contacto.getImageLocation() );

                startActivity( intent );
            }
        } );

        return view;
    }

}
